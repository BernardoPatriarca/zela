package br.edu.unimater.zela.prestacaocontas;

import br.edu.unimater.zela.caso.Caso;
import br.edu.unimater.zela.caso.PeriodoPrestacao;
import br.edu.unimater.zela.contabancaria.ContaBancaria;
import br.edu.unimater.zela.contabancaria.ContaBancariaRepository;
import br.edu.unimater.zela.lancamento.Lancamento;
import br.edu.unimater.zela.lancamento.LancamentoRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@ApplicationScoped
public class DadosPeriodoService {

    @Inject
    ContaBancariaRepository contaBancariaRepository;

    @Inject
    LancamentoRepository lancamentoRepository;

    @Inject
    SaldoService saldoService;

    public List<ContaBancaria> contasDoCaso(Caso caso) {
        return contaBancariaRepository.listar(caso.id, 0, 100).itens();
    }

    public DadosPeriodo carregar(Caso caso, PeriodoPrestacao periodo) {
        List<ContaBancaria> contas = contasDoCaso(caso);
        List<UUID> contasIds = contas.stream().map(conta -> conta.id).toList();
        List<Lancamento> lancamentos = contas.isEmpty()
                ? List.of()
                : lancamentoRepository.listarDoPeriodo(contasIds, periodo.dataInicio, periodo.dataFim);

        Map<UUID, List<Lancamento>> lancamentosPorConta = new HashMap<>();
        for (Lancamento lancamento : lancamentos) {
            lancamentosPorConta.computeIfAbsent(lancamento.contaBancaria.id, id -> new ArrayList<>())
                    .add(lancamento);
        }

        return new DadosPeriodo(contas, saldoService.listarSaldosIniciais(periodo.id), lancamentos,
                lancamentosPorConta);
    }
}
