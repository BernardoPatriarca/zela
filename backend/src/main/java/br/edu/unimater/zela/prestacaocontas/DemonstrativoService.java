package br.edu.unimater.zela.prestacaocontas;

import br.edu.unimater.zela.caso.Caso;
import br.edu.unimater.zela.caso.PeriodoPrestacao;
import br.edu.unimater.zela.caso.TipoCaso;
import br.edu.unimater.zela.categoria.TipoCategoria;
import br.edu.unimater.zela.contabancaria.ContaBancaria;
import br.edu.unimater.zela.importacaoextrato.ImportacaoExtrato;
import br.edu.unimater.zela.importacaoextrato.ImportacaoExtratoRepository;
import br.edu.unimater.zela.lancamento.Lancamento;
import br.edu.unimater.zela.pessoa.Pessoa;
import br.edu.unimater.zela.prestacaocontas.dto.ConciliacaoContaDto;
import br.edu.unimater.zela.prestacaocontas.dto.ConferenciaAritmeticaDto;
import br.edu.unimater.zela.prestacaocontas.dto.DemonstrativoDto;
import br.edu.unimater.zela.prestacaocontas.dto.DocumentoPrestacaoDto;
import br.edu.unimater.zela.prestacaocontas.dto.LinhaConsolidadoDto;
import br.edu.unimater.zela.prestacaocontas.dto.LinhaPlanilhaDto;
import br.edu.unimater.zela.prestacaocontas.dto.MesPlanilhaDto;
import br.edu.unimater.zela.prestacaocontas.dto.PlanilhaContaDto;
import br.edu.unimater.zela.prestacaocontas.dto.RessalvaDto;
import br.edu.unimater.zela.shared.exception.BusinessRuleException;
import br.edu.unimater.zela.shared.util.Formato;
import br.edu.unimater.zela.shared.util.Mascara;
import br.edu.unimater.zela.shared.util.NumeroCnj;
import br.edu.unimater.zela.usuario.Usuario;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@ApplicationScoped
public class DemonstrativoService {

    private static final ZoneId FUSO = ZoneId.of("America/Sao_Paulo");
    private static final Locale PT_BR = Locale.of("pt", "BR");
    private static final String CPF_OCULTO = "***.***.***-**";
    private static final String SEM_EMITENTE = "—";

    @Inject
    ImportacaoExtratoRepository importacaoRepository;

    @Inject
    SaldoService saldoService;

    public DemonstrativoDto montar(Caso caso, PeriodoPrestacao periodo, Usuario advogado, DadosPeriodo dados,
                                   List<RessalvaDto> ressalvas, String notasExplicativas) {
        List<ContaBancaria> contas = dados.contas();
        if (contas.isEmpty()) {
            throw new BusinessRuleException("Cadastre ao menos uma conta bancária para gerar o relatório.");
        }
        saldoService.exigirSaldosIniciais(dados.saldosIniciais(), contas);
        List<UUID> contasIds = contas.stream().map(conta -> conta.id).toList();
        Map<UUID, BigDecimal> saldosIniciais = dados.saldosIniciais();
        Map<UUID, List<Lancamento>> lancamentosPorConta = dados.lancamentosPorConta();

        Map<UUID, String> numeros = numerar(dados.lancamentos());

        List<YearMonth> meses = listarMeses(periodo);
        Map<YearMonth, BigDecimal> receitasDoMes = new HashMap<>();
        Map<YearMonth, BigDecimal> despesasDoMes = new HashMap<>();
        Map<YearMonth, BigDecimal> saldoDoMes = new HashMap<>();

        List<PlanilhaContaDto> planilhas = new ArrayList<>();
        List<ConferenciaAritmeticaDto> conferencias = new ArrayList<>();
        BigDecimal inicialTotal = BigDecimal.ZERO;
        BigDecimal receitasTotal = BigDecimal.ZERO;
        BigDecimal despesasTotal = BigDecimal.ZERO;
        BigDecimal finalTotal = BigDecimal.ZERO;

        for (ContaBancaria conta : contas) {
            List<Lancamento> doConta = lancamentosPorConta.getOrDefault(conta.id, List.of());
            BigDecimal saldoInicial = saldosIniciais.get(conta.id);
            BigDecimal saldo = saldoInicial;
            BigDecimal receitasConta = BigDecimal.ZERO;
            BigDecimal despesasConta = BigDecimal.ZERO;
            List<MesPlanilhaDto> mesesDaConta = new ArrayList<>();

            for (YearMonth mes : meses) {
                BigDecimal saldoAnterior = saldo;
                BigDecimal receitas = BigDecimal.ZERO;
                BigDecimal despesas = BigDecimal.ZERO;
                List<LinhaPlanilhaDto> linhas = new ArrayList<>();

                for (Lancamento lancamento : doConta) {
                    if (!YearMonth.from(lancamento.dataLancamento).equals(mes)) {
                        continue;
                    }
                    boolean receita = lancamento.tipo == TipoCategoria.RECEITA;
                    if (receita) {
                        receitas = receitas.add(lancamento.valor);
                        saldo = saldo.add(lancamento.valor);
                    } else {
                        despesas = despesas.add(lancamento.valor);
                        saldo = saldo.subtract(lancamento.valor);
                    }
                    linhas.add(new LinhaPlanilhaDto(
                            numeros.get(lancamento.id),
                            Formato.data(lancamento.dataLancamento),
                            emitente(lancamento),
                            lancamento.descricao,
                            lancamento.categoria.nome,
                            receita ? Formato.moeda(lancamento.valor) : "",
                            receita ? "" : Formato.moeda(lancamento.valor),
                            Formato.moeda(saldo)));
                }

                mesesDaConta.add(new MesPlanilhaDto(tituloDoMes(mes, periodo), Formato.moeda(saldoAnterior), linhas,
                        Formato.moeda(receitas), Formato.moeda(despesas), Formato.moeda(saldo)));
                receitasConta = receitasConta.add(receitas);
                despesasConta = despesasConta.add(despesas);
                receitasDoMes.merge(mes, receitas, BigDecimal::add);
                despesasDoMes.merge(mes, despesas, BigDecimal::add);
                saldoDoMes.merge(mes, saldo, BigDecimal::add);
            }

            planilhas.add(new PlanilhaContaDto(rotuloConta(conta), mesesDaConta));
            conferencias.add(new ConferenciaAritmeticaDto(rotuloConta(conta), Formato.moeda(saldoInicial),
                    Formato.moeda(receitasConta), Formato.moeda(despesasConta), Formato.moeda(saldo)));
            inicialTotal = inicialTotal.add(saldoInicial);
            receitasTotal = receitasTotal.add(receitasConta);
            despesasTotal = despesasTotal.add(despesasConta);
            finalTotal = finalTotal.add(saldo);
        }

        if (contas.size() > 1) {
            conferencias.add(new ConferenciaAritmeticaDto("Total das contas", Formato.moeda(inicialTotal),
                    Formato.moeda(receitasTotal), Formato.moeda(despesasTotal), Formato.moeda(finalTotal)));
        }

        List<LinhaConsolidadoDto> consolidado = new ArrayList<>();
        for (YearMonth mes : meses) {
            consolidado.add(new LinhaConsolidadoDto(tituloDoMes(mes, periodo), Formato.moeda(receitasDoMes.get(mes)),
                    Formato.moeda(despesasDoMes.get(mes)), Formato.moeda(saldoDoMes.get(mes))));
        }
        LinhaConsolidadoDto totalConsolidado = new LinhaConsolidadoDto("Período", Formato.moeda(receitasTotal),
                Formato.moeda(despesasTotal), Formato.moeda(finalTotal));

        List<ConciliacaoContaDto> conciliacoes = conciliar(contas, contasIds, periodo, saldosIniciais,
                lancamentosPorConta);

        DocumentoPrestacaoDto documento = montarDocumento(caso, periodo, advogado, finalTotal, planilhas, consolidado,
                totalConsolidado, conferencias, conciliacoes, ressalvas, notasExplicativas);
        return new DemonstrativoDto(documento, finalTotal);
    }

    private DocumentoPrestacaoDto montarDocumento(Caso caso, PeriodoPrestacao periodo, Usuario advogado,
                                                  BigDecimal saldoFinalTotal, List<PlanilhaContaDto> planilhas,
                                                  List<LinhaConsolidadoDto> consolidado,
                                                  LinhaConsolidadoDto totalConsolidado,
                                                  List<ConferenciaAritmeticaDto> conferencias,
                                                  List<ConciliacaoContaDto> conciliacoes,
                                                  List<RessalvaDto> ressalvas, String notasExplicativas) {
        boolean tutela = caso.tipo == TipoCaso.TUTELA;
        Pessoa protegida = caso.pessoaProtegida;
        Pessoa responsavel = caso.responsavel;
        LocalDate hoje = LocalDate.now(FUSO);

        String nomeProtegido = tutela ? Mascara.iniciais(protegida.nome) : protegida.nome;
        String protegidoComIdade = nomeProtegido + ", " + Period.between(protegida.dataNascimento, hoje).getYears()
                + " anos";
        String cpfProtegido = tutela ? CPF_OCULTO : cpfOuNaoInformado(protegida.cpf);

        String comarca = caso.vara.comarca.nome + "/" + caso.vara.comarca.uf;
        String fundamentacao = tutela
                ? "art. 1.757 do Código Civil"
                : "art. 1.757 c/c art. 1.774 do Código Civil e art. 84, §4º, da Lei 13.146/2015";

        return new DocumentoPrestacaoDto(
                caso.vara.nome.toUpperCase(PT_BR),
                comarca.toUpperCase(PT_BR),
                caso.vara.nome + " da Comarca de " + comarca,
                NumeroCnj.formatar(caso.numeroProcesso),
                tutela ? "Tutelado(a)" : "Curatelado(a)",
                protegidoComIdade,
                cpfProtegido,
                tutela ? "Tutor(a)" : "Curador(a)",
                responsavel.nome,
                cpfOuNaoInformado(responsavel.cpf),
                advogado.nomeCompleto,
                "OAB/" + advogado.oabUf + " " + advogado.oabNumero,
                Formato.data(periodo.dataInicio) + " a " + Formato.data(periodo.dataFim),
                periodicidade(caso.periodicidadeMeses),
                fundamentacao,
                Formato.moeda(saldoFinalTotal),
                !ressalvas.isEmpty(),
                caso.vara.comarca.nome,
                Formato.dataExtensa(hoje),
                planilhas,
                consolidado,
                totalConsolidado,
                conferencias,
                conciliacoes,
                ressalvas,
                notasExplicativas == null || notasExplicativas.isBlank() ? null : notasExplicativas.trim());
    }

    private List<ConciliacaoContaDto> conciliar(List<ContaBancaria> contas, List<UUID> contasIds,
                                                PeriodoPrestacao periodo, Map<UUID, BigDecimal> saldosIniciais,
                                                Map<UUID, List<Lancamento>> lancamentosPorConta) {
        Map<UUID, ImportacaoExtrato> extratos = new LinkedHashMap<>();
        for (ImportacaoExtrato importacao : importacaoRepository.listarComSaldoNoPeriodo(contasIds,
                periodo.dataInicio, periodo.dataFim)) {
            extratos.putIfAbsent(importacao.contaBancaria.id, importacao);
        }

        List<ConciliacaoContaDto> conciliacoes = new ArrayList<>();
        for (ContaBancaria conta : contas) {
            ImportacaoExtrato extrato = extratos.get(conta.id);
            if (extrato == null) {
                conciliacoes.add(new ConciliacaoContaDto(rotuloConta(conta), false, null, null, null, null));
                continue;
            }
            BigDecimal calculado = saldoService.saldoEm(saldosIniciais.get(conta.id),
                    lancamentosPorConta.getOrDefault(conta.id, List.of()), extrato.dataSaldoFinal);
            conciliacoes.add(new ConciliacaoContaDto(rotuloConta(conta), true, Formato.data(extrato.dataSaldoFinal),
                    Formato.moeda(calculado), Formato.moeda(extrato.saldoFinal),
                    Formato.moeda(extrato.saldoFinal.subtract(calculado))));
        }
        return conciliacoes;
    }

    private Map<UUID, String> numerar(List<Lancamento> lancamentos) {
        Map<UUID, String> numeros = new HashMap<>();
        int receitas = 0;
        int despesas = 0;
        for (Lancamento lancamento : lancamentos) {
            if (lancamento.tipo == TipoCategoria.RECEITA) {
                numeros.put(lancamento.id, String.format("E-%02d", ++receitas));
            } else {
                numeros.put(lancamento.id, String.format("D-%02d", ++despesas));
            }
        }
        return numeros;
    }

    private List<YearMonth> listarMeses(PeriodoPrestacao periodo) {
        List<YearMonth> meses = new ArrayList<>();
        YearMonth ultimo = YearMonth.from(periodo.dataFim);
        for (YearMonth mes = YearMonth.from(periodo.dataInicio); !mes.isAfter(ultimo); mes = mes.plusMonths(1)) {
            meses.add(mes);
        }
        return meses;
    }

    private String tituloDoMes(YearMonth mes, PeriodoPrestacao periodo) {
        String nome = Formato.nomeMes(mes.getMonth());
        String titulo = nome.substring(0, 1).toUpperCase(PT_BR) + nome.substring(1) + "/" + mes.getYear();
        LocalDate inicio = periodo.dataInicio.isAfter(mes.atDay(1)) ? periodo.dataInicio : mes.atDay(1);
        LocalDate fim = periodo.dataFim.isBefore(mes.atEndOfMonth()) ? periodo.dataFim : mes.atEndOfMonth();
        if (inicio.equals(mes.atDay(1)) && fim.equals(mes.atEndOfMonth())) {
            return titulo;
        }
        return titulo + " (de " + Formato.data(inicio) + " a " + Formato.data(fim) + ")";
    }

    private String rotuloConta(ContaBancaria conta) {
        String rotulo = conta.banco.nome + " · Ag. " + conta.agencia + " · Conta " + conta.numero;
        return conta.descricao == null ? rotulo : rotulo + " — " + conta.descricao;
    }

    private String periodicidade(int meses) {
        return switch (meses) {
            case 1 -> "Mensal";
            case 3 -> "Trimestral";
            case 6 -> "Semestral";
            case 12 -> "Anual";
            default -> "A cada " + meses + " meses";
        };
    }

    private String cpfOuNaoInformado(String cpf) {
        String mascarado = Mascara.cpf(cpf);
        return mascarado == null ? "não informado" : mascarado;
    }

    private String emitente(Lancamento lancamento) {
        String documento = lancamento.emitenteDocumento == null
                ? null
                : (lancamento.emitenteDocumento.length() == 14 ? "CNPJ " : "CPF ")
                + Mascara.documento(lancamento.emitenteDocumento);
        if (lancamento.emitenteNome == null) {
            return documento == null ? SEM_EMITENTE : documento;
        }
        return documento == null ? lancamento.emitenteNome : lancamento.emitenteNome + " — " + documento;
    }
}
