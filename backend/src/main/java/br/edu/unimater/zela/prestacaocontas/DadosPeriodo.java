package br.edu.unimater.zela.prestacaocontas;

import br.edu.unimater.zela.contabancaria.ContaBancaria;
import br.edu.unimater.zela.lancamento.Lancamento;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record DadosPeriodo(
        List<ContaBancaria> contas,
        Map<UUID, BigDecimal> saldosIniciais,
        List<Lancamento> lancamentos,
        Map<UUID, List<Lancamento>> lancamentosPorConta
) {}
