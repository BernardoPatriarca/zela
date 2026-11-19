package br.edu.unimater.zela.shared.util;

public final class Documento {

    private static final int[] PESOS_CNPJ_1 = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
    private static final int[] PESOS_CNPJ_2 = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};

    private Documento() {}

    public static boolean valido(String digitos) {
        if (digitos == null || !digitos.matches("\\d+") || digitos.chars().distinct().count() == 1) {
            return false;
        }
        return switch (digitos.length()) {
            case 11 -> cpfValido(digitos);
            case 14 -> cnpjValido(digitos);
            default -> false;
        };
    }

    private static boolean cpfValido(String cpf) {
        return digitoCpf(cpf, 9) == cpf.charAt(9) - '0' && digitoCpf(cpf, 10) == cpf.charAt(10) - '0';
    }

    private static int digitoCpf(String cpf, int quantidade) {
        int soma = 0;
        for (int i = 0; i < quantidade; i++) {
            soma += (cpf.charAt(i) - '0') * (quantidade + 1 - i);
        }
        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }

    private static boolean cnpjValido(String cnpj) {
        return digitoCnpj(cnpj, PESOS_CNPJ_1) == cnpj.charAt(12) - '0'
                && digitoCnpj(cnpj, PESOS_CNPJ_2) == cnpj.charAt(13) - '0';
    }

    private static int digitoCnpj(String cnpj, int[] pesos) {
        int soma = 0;
        for (int i = 0; i < pesos.length; i++) {
            soma += (cnpj.charAt(i) - '0') * pesos[i];
        }
        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }
}
