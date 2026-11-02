package br.edu.unimater.zela.shared.dto;

import io.quarkus.hibernate.orm.panache.PanacheQuery;

import java.util.List;
import java.util.function.Function;

public record Pagina<T>(
        List<T> itens,
        long total,
        int pagina,
        int tamanho
) {
    public static <T> Pagina<T> de(PanacheQuery<T> query, int pagina, int tamanho) {
        return new Pagina<>(query.page(pagina, tamanho).list(), query.count(), pagina, tamanho);
    }

    public <R> Pagina<R> map(Function<T, R> conversor) {
        return new Pagina<>(itens.stream().map(conversor).toList(), total, pagina, tamanho);
    }
}
