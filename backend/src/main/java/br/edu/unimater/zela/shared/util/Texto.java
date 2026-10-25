package br.edu.unimater.zela.shared.util;

public final class Texto {

    private Texto() {}

    public static String trimOuNulo(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }

    public static String apenasDigitos(String valor) {
        if (valor == null) {
            return null;
        }
        String digitos = valor.replaceAll("\\D", "");
        return digitos.isEmpty() ? null : digitos;
    }
}