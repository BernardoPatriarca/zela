package br.edu.unimater.zela.prestacaocontas;

import br.edu.unimater.zela.categoria.TipoCategoria;
import br.edu.unimater.zela.contabancaria.ContaBancaria;
import br.edu.unimater.zela.lancamento.Lancamento;
import br.edu.unimater.zela.shared.exception.BusinessRuleException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@ApplicationScoped
public class SaldoService {

    @Inject
    SaldoPeriodoRepository repository;

    public Map<UUID, BigDecimal> listarSaldosIniciais(UUID periodoId) {
        Map<UUID, BigDecimal> saldos = new HashMap<>();
        for (SaldoPeriodo saldo : repository.listarPorPeriodo(periodoId)) {
            saldos.put(saldo.contaBancariaId, saldo.saldoInicial);
        }
        return saldos;
    }

    public void exigirSaldosIniciais(Map<UUID, BigDecimal> saldos, List<ContaBancaria> contas) {
        for (ContaBancaria conta : contas) {
            if (!saldos.containsKey(conta.id)) {
                throw new BusinessRuleException("Informe o saldo inicial de todas as contas.");
            }
        }
    }

    public BigDecimal saldoEm(BigDecimal saldoInicial, List<Lancamento> lancamentos, LocalDate data) {
        BigDecimal saldo = saldoInicial;
        for (Lancamento lancamento : lancamentos) {
            if (lancamento.dataLancamento.isAfter(data)) {
                continue;
            }
            saldo = lancamento.tipo == TipoCategoria.RECEITA
                    ? saldo.add(lancamento.valor)
                    : saldo.subtract(lancamento.valor);
        }
        return saldo;
    }
}
