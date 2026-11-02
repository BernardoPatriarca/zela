package br.edu.unimater.zela.shared.util;

import java.math.BigInteger;

public final class NumeroCnj {

    private static final BigInteger NOVENTA_E_SETE = BigInteger.valueOf(97);

    private NumeroCnj() {}

    public static boolean valido(String numero) {
        if (numero == null || !numero.matches("\\d{20}")) {
            return false;
        }

        String sequencial = numero.substring(0, 7);
        int digito = Integer.parseInt(numero.substring(7, 9));
        String anoOrgaoOrigem = numero.substring(9);

        BigInteger base = new BigInteger(sequencial + anoOrgaoOrigem + "00");
        return 98 - base.mod(NOVENTA_E_SETE).intValue() == digito;
    }

    public static String formatar(String numero) {
        return numero.replaceFirst("(\\d{7})(\\d{2})(\\d{4})(\\d)(\\d{2})(\\d{4})", "$1-$2.$3.$4.$5.$6");
    }
}
