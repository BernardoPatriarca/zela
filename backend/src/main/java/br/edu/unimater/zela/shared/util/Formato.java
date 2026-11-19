package br.edu.unimater.zela.shared.util;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;

public final class Formato {

    private static final Locale PT_BR = Locale.of("pt", "BR");
    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private Formato() {}

    public static String moeda(BigDecimal valor) {
        return NumberFormat.getCurrencyInstance(PT_BR).format(valor);
    }

    public static String data(LocalDate data) {
        return DATA.format(data);
    }

    public static String dataExtensa(LocalDate data) {
        return data.getDayOfMonth() + " de " + nomeMes(data.getMonth()) + " de " + data.getYear();
    }

    public static String nomeMes(Month mes) {
        return mes.getDisplayName(TextStyle.FULL, PT_BR);
    }
}
