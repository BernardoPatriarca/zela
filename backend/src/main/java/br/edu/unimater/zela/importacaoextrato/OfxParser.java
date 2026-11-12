package br.edu.unimater.zela.importacaoextrato;

import br.edu.unimater.zela.shared.exception.BusinessRuleException;
import br.edu.unimater.zela.shared.exception.ErrorResponse;
import br.edu.unimater.zela.shared.util.Texto;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class OfxParser {

    private static final String ARQUIVO_INVALIDO = "Arquivo OFX incompleto ou inválido.";
    private static final int MAXIMO_TRANSACOES_INVALIDAS = 50;

    private static final Pattern ABERTURA_OFX = Pattern.compile("<OFX>", Pattern.CASE_INSENSITIVE);
    private static final Pattern TAG = Pattern.compile("<(/?)([A-Za-z0-9_.]+)>([^<\\r\\n]*)");
    private static final Pattern PROLOGO_XML = Pattern.compile("<\\?xml[^>]*encoding=[\"']([^\"']+)[\"']",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern VALOR = Pattern.compile("^[+-]?(\\d+\\.?\\d*|\\.\\d+)$");
    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("uuuuMMdd")
            .withResolverStyle(ResolverStyle.STRICT);

    private OfxParser() {}

    public static OfxExtrato ler(byte[] conteudo) {
        String texto = decodificar(conteudo);

        boolean temOfx = false;
        boolean temExtrato = false;
        boolean temCartao = false;
        boolean abriuLista = false;
        boolean fechouLista = false;
        String agregado = null;
        Map<String, String> atual = null;
        List<Map<String, String>> brutas = new ArrayList<>();

        String bancoId = null;
        String agenciaId = null;
        String contaId = null;
        String inicio = null;
        String fim = null;
        String saldo = null;
        String dataSaldo = null;

        Matcher tags = TAG.matcher(texto);
        while (tags.find()) {
            boolean fechamento = !tags.group(1).isEmpty();
            String tag = tags.group(2).toUpperCase();
            String valor = desescapar(tags.group(3).trim());

            if (fechamento) {
                switch (tag) {
                    case "STMTTRN" -> {
                        if (atual == null) {
                            throw new BusinessRuleException(ARQUIVO_INVALIDO);
                        }
                        brutas.add(atual);
                        atual = null;
                    }
                    case "BANKTRANLIST" -> {
                        if (!abriuLista || atual != null) {
                            throw new BusinessRuleException(ARQUIVO_INVALIDO);
                        }
                        fechouLista = true;
                    }
                    case "BANKACCTFROM", "LEDGERBAL", "AVAILBAL" -> agregado = null;
                    default -> {
                    }
                }
                continue;
            }

            if (valor.isEmpty()) {
                switch (tag) {
                    case "OFX" -> temOfx = true;
                    case "STMTRS" -> temExtrato = true;
                    case "CCSTMTRS" -> temCartao = true;
                    case "BANKTRANLIST" -> abriuLista = true;
                    case "STMTTRN" -> {
                        if (!abriuLista || fechouLista || atual != null) {
                            throw new BusinessRuleException(ARQUIVO_INVALIDO);
                        }
                        atual = new HashMap<>();
                    }
                    case "BANKACCTFROM", "LEDGERBAL", "AVAILBAL" -> agregado = tag;
                    default -> {
                    }
                }
                continue;
            }

            if (atual != null) {
                atual.putIfAbsent(tag, valor);
                continue;
            }

            switch (tag) {
                case "BANKID" -> bancoId = "BANKACCTFROM".equals(agregado) ? valor : bancoId;
                case "BRANCHID" -> agenciaId = "BANKACCTFROM".equals(agregado) ? valor : agenciaId;
                case "ACCTID" -> contaId = "BANKACCTFROM".equals(agregado) ? valor : contaId;
                case "DTSTART" -> inicio = abriuLista && !fechouLista ? valor : inicio;
                case "DTEND" -> fim = abriuLista && !fechouLista ? valor : fim;
                case "BALAMT" -> saldo = "LEDGERBAL".equals(agregado) ? valor : saldo;
                case "DTASOF" -> dataSaldo = "LEDGERBAL".equals(agregado) ? valor : dataSaldo;
                default -> {
                }
            }
        }

        if (!temOfx) {
            throw new BusinessRuleException(ARQUIVO_INVALIDO);
        }
        if (!temExtrato && temCartao) {
            throw new BusinessRuleException("Faturas de cartão não são aceitas. Importe o extrato da conta.");
        }
        if (!temExtrato || !abriuLista || !fechouLista || atual != null) {
            throw new BusinessRuleException(ARQUIVO_INVALIDO);
        }
        if (brutas.isEmpty()) {
            throw new BusinessRuleException("O arquivo OFX não possui movimentações.");
        }

        List<OfxTransacao> transacoes = converterTransacoes(brutas);

        LocalDate dataInicio = lerData(inicio);
        LocalDate dataFim = lerData(fim);
        if ((inicio != null && dataInicio == null) || (fim != null && dataFim == null)) {
            throw new BusinessRuleException(ARQUIVO_INVALIDO);
        }
        if (dataInicio == null) {
            dataInicio = transacoes.stream().map(OfxTransacao::data).min(LocalDate::compareTo).orElseThrow();
        }
        if (dataFim == null) {
            dataFim = transacoes.stream().map(OfxTransacao::data).max(LocalDate::compareTo).orElseThrow();
        }
        if (dataFim.isBefore(dataInicio)) {
            throw new BusinessRuleException(ARQUIVO_INVALIDO);
        }

        BigDecimal saldoFinal = saldo == null ? null : lerValor(saldo);
        LocalDate dataSaldoFinal = saldoFinal == null ? null : lerData(dataSaldo);

        return new OfxExtrato(
                limitar(bancoId, 20),
                limitar(agenciaId, 30),
                limitar(contaId, 30),
                dataInicio,
                dataFim,
                saldoFinal,
                dataSaldoFinal,
                transacoes
        );
    }

    private static List<OfxTransacao> converterTransacoes(List<Map<String, String>> brutas) {
        List<OfxTransacao> transacoes = new ArrayList<>();
        List<ErrorResponse.FieldError> invalidas = new ArrayList<>();
        int totalInvalidas = 0;

        for (int i = 0; i < brutas.size(); i++) {
            Map<String, String> bruta = brutas.get(i);
            int posicao = i + 1;
            String fitid = limitar(Texto.trimOuNulo(bruta.get("FITID")), 255);

            List<String> motivos = new ArrayList<>();
            LocalDate data = lerData(bruta.get("DTPOSTED"));
            if (data == null) {
                motivos.add("Data inválida.");
            }
            BigDecimal valor = null;
            String valorBruto = bruta.get("TRNAMT");
            if (valorBruto == null) {
                motivos.add("Valor não informado.");
            } else {
                valor = lerValor(valorBruto);
                if (valor == null) {
                    motivos.add("Valor inválido.");
                }
            }

            if (!motivos.isEmpty()) {
                totalInvalidas++;
                if (invalidas.size() < MAXIMO_TRANSACOES_INVALIDAS) {
                    String campo = fitid == null ? "Transação " + posicao
                            : "Transação " + posicao + " (FITID " + fitid + ")";
                    invalidas.add(new ErrorResponse.FieldError(campo, String.join(" ", motivos)));
                }
                continue;
            }

            String descricao = Texto.trimOuNulo(bruta.get("MEMO"));
            if (descricao == null) {
                descricao = Texto.trimOuNulo(bruta.get("NAME"));
            }
            if (descricao == null) {
                descricao = "Sem descrição";
            }

            transacoes.add(new OfxTransacao(
                    posicao,
                    limitar(Texto.trimOuNulo(bruta.get("TRNTYPE")), 20),
                    data,
                    valor,
                    fitid,
                    limitar(descricao, 255),
                    limitar(Texto.trimOuNulo(bruta.get("CHECKNUM")), 50)
            ));
        }

        if (totalInvalidas > 0) {
            String mensagem = "O arquivo possui transações inválidas e nada foi importado.";
            if (totalInvalidas > invalidas.size()) {
                mensagem += " Exibindo as primeiras " + invalidas.size() + ".";
            }
            throw new BusinessRuleException(mensagem, invalidas);
        }
        return transacoes;
    }

    private static String decodificar(byte[] conteudo) {
        String latin1 = new String(conteudo, StandardCharsets.ISO_8859_1);
        Matcher abertura = ABERTURA_OFX.matcher(latin1);
        if (!abertura.find()) {
            throw new BusinessRuleException(ARQUIVO_INVALIDO);
        }
        int inicio = abertura.start();
        Charset charset = descobrirCharset(latin1.substring(0, inicio));
        return new String(conteudo, inicio, conteudo.length - inicio, charset);
    }

    private static Charset descobrirCharset(String cabecalho) {
        Matcher prologo = PROLOGO_XML.matcher(cabecalho);
        if (prologo.find()) {
            try {
                return Charset.forName(prologo.group(1));
            } catch (IllegalArgumentException e) {
                return StandardCharsets.UTF_8;
            }
        }
        if (cabecalho.toLowerCase().contains("<?xml")) {
            return StandardCharsets.UTF_8;
        }

        String charset = valorDoCabecalho(cabecalho, "CHARSET");
        String encoding = valorDoCabecalho(cabecalho, "ENCODING");
        if (charset.equals("ISO-8859-1") || charset.equals("8859-1")) {
            return StandardCharsets.ISO_8859_1;
        }
        if (charset.equals("UTF-8") || (charset.equals("NONE") && encoding.equals("UTF-8"))) {
            return StandardCharsets.UTF_8;
        }
        return Charset.forName("windows-1252");
    }

    private static String valorDoCabecalho(String cabecalho, String chave) {
        Matcher linha = Pattern.compile("(?im)^\\s*" + chave + ":([^\\r\\n]*)").matcher(cabecalho);
        return linha.find() ? linha.group(1).trim().toUpperCase() : "";
    }

    private static LocalDate lerData(String texto) {
        if (texto == null) {
            return null;
        }
        String digitos = texto.replaceAll("\\D", "");
        if (digitos.length() < 8) {
            return null;
        }
        try {
            return LocalDate.parse(digitos.substring(0, 8), FORMATO_DATA);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private static BigDecimal lerValor(String texto) {
        String limpo = texto.trim().replace(" ", "");
        if (limpo.contains(",")) {
            limpo = limpo.replace(".", "").replace(",", ".");
        }
        if (!VALOR.matcher(limpo).matches()) {
            return null;
        }
        BigDecimal valor = new BigDecimal(limpo);
        if (valor.stripTrailingZeros().scale() > 2) {
            return null;
        }
        valor = valor.setScale(2, RoundingMode.UNNECESSARY);
        if (valor.precision() - valor.scale() > 13) {
            return null;
        }
        return valor;
    }

    private static String desescapar(String texto) {
        return texto.replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&quot;", "\"")
                .replace("&apos;", "'")
                .replace("&amp;", "&");
    }

    private static String limitar(String texto, int tamanho) {
        return texto == null || texto.length() <= tamanho ? texto : texto.substring(0, tamanho);
    }
}
