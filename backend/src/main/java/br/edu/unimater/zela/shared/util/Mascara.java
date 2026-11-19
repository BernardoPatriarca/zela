package br.edu.unimater.zela.shared.util;

import java.util.Set;

public final class Mascara {

    private static final Set<String> PARTICULAS = Set.of("de", "da", "do", "das", "dos", "e");

    private Mascara() {}

    public static String cpf(String digitos) {
        if (digitos == null || digitos.length() != 11) {
            return null;
        }
        return "***." + digitos.substring(3, 6) + "." + digitos.substring(6, 9) + "-**";
    }

    public static String cnpj(String digitos) {
        if (digitos == null || digitos.length() != 14) {
            return null;
        }
        return "**.***.***/" + digitos.substring(8, 12) + "-" + digitos.substring(12, 14);
    }

    public static String documento(String digitos) {
        if (digitos == null) {
            return null;
        }
        return digitos.length() == 14 ? cnpj(digitos) : cpf(digitos);
    }

    public static String iniciais(String nome) {
        StringBuilder iniciais = new StringBuilder();
        for (String parte : nome.trim().split("\\s+")) {
            if (parte.isEmpty() || PARTICULAS.contains(parte.toLowerCase())) {
                continue;
            }
            if (iniciais.length() > 0) {
                iniciais.append(' ');
            }
            iniciais.append(parte.substring(0, 1).toUpperCase()).append('.');
        }
        return iniciais.toString();
    }
}
