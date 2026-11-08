package br.edu.unimater.zela.lancamento;

import br.edu.unimater.zela.caso.Caso;
import br.edu.unimater.zela.caso.CasoRepository;
import br.edu.unimater.zela.categoria.Categoria;
import br.edu.unimater.zela.categoria.CategoriaRepository;
import br.edu.unimater.zela.categoria.TipoCategoria;
import br.edu.unimater.zela.contabancaria.ContaBancaria;
import br.edu.unimater.zela.contabancaria.ContaBancariaRepository;
import br.edu.unimater.zela.lancamento.dto.LancamentoCreateDto;
import br.edu.unimater.zela.lancamento.dto.LancamentoExclusaoDto;
import br.edu.unimater.zela.lancamento.dto.LancamentoResponseDto;
import br.edu.unimater.zela.lancamento.dto.LancamentoUpdateDto;
import br.edu.unimater.zela.shared.dto.Pagina;
import br.edu.unimater.zela.shared.exception.BusinessRuleException;
import br.edu.unimater.zela.shared.exception.ResourceNotFoundException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@ApplicationScoped
public class LancamentoService {

    @Inject
    LancamentoRepository repository;

    @Inject
    CasoRepository casoRepository;

    @Inject
    ContaBancariaRepository contaBancariaRepository;

    @Inject
    CategoriaRepository categoriaRepository;

    @Inject
    JsonWebToken jwt;

    public Pagina<LancamentoResponseDto> listar(UUID casoId, LocalDate dataInicio, LocalDate dataFim,
                                                UUID contaBancariaId, TipoCategoria tipo, UUID categoriaId,
                                                SituacaoLancamento situacao, int pagina, int tamanho) {
        Caso caso = buscarCaso(casoId);
        if (dataInicio != null && dataFim != null && dataInicio.isAfter(dataFim)) {
            throw new BusinessRuleException("A data inicial não pode ser posterior à data final.");
        }
        return repository.listar(caso.id, dataInicio, dataFim, contaBancariaId, tipo, categoriaId, situacao,
                pagina, tamanho).map(LancamentoResponseDto::from);
    }

    public LancamentoResponseDto buscarPorId(UUID casoId, UUID id) {
        return LancamentoResponseDto.from(buscar(buscarCaso(casoId), id));
    }

    @Transactional
    public LancamentoResponseDto criar(UUID casoId, LancamentoCreateDto dto) {
        Caso caso = buscarCasoEditavel(casoId);
        ContaBancaria conta = buscarConta(caso, dto.contaBancariaId());
        Categoria categoria = buscarCategoria(dto.categoriaId(), dto.tipo());
        validarData(dto.dataLancamento(), conta);

        Lancamento lancamento = new Lancamento();
        lancamento.contaBancaria = conta;
        lancamento.categoria = categoria;
        lancamento.tipo = dto.tipo();
        lancamento.dataLancamento = dto.dataLancamento();
        lancamento.valor = dto.valor();
        lancamento.descricao = dto.descricao().trim();
        lancamento.situacao = SituacaoLancamento.PENDENTE;

        repository.persistAndFlush(lancamento);
        return LancamentoResponseDto.from(lancamento);
    }

    @Transactional
    public LancamentoResponseDto atualizar(UUID casoId, UUID id, LancamentoUpdateDto dto) {
        Caso caso = buscarCasoEditavel(casoId);
        Lancamento lancamento = buscar(caso, id);
        ContaBancaria conta = buscarConta(caso, dto.contaBancariaId());
        Categoria categoria = buscarCategoria(dto.categoriaId(), lancamento.tipo);
        validarData(dto.dataLancamento(), conta);

        lancamento.contaBancaria = conta;
        lancamento.categoria = categoria;
        lancamento.dataLancamento = dto.dataLancamento();
        lancamento.valor = dto.valor();
        lancamento.descricao = dto.descricao().trim();
        lancamento.justificativaAlteracao = dto.justificativa().trim();

        repository.flush();
        return LancamentoResponseDto.from(lancamento);
    }

    @Transactional
    public void excluir(UUID casoId, UUID id, LancamentoExclusaoDto dto) {
        Lancamento lancamento = buscar(buscarCasoEditavel(casoId), id);
        lancamento.justificativaAlteracao = dto.justificativa().trim();
        lancamento.excluidoEm = Instant.now();
    }

    private Caso buscarCaso(UUID casoId) {
        return casoRepository.buscarPorId(casoId, usuarioLogado())
                .orElseThrow(() -> new ResourceNotFoundException("Caso não encontrado."));
    }

    private Caso buscarCasoEditavel(UUID casoId) {
        Caso caso = buscarCaso(casoId);
        if (caso.isEncerrado()) {
            throw new BusinessRuleException("Reabra o caso para alterar os lançamentos.");
        }
        return caso;
    }

    private Lancamento buscar(Caso caso, UUID id) {
        return repository.buscarPorId(id, caso.id)
                .orElseThrow(() -> new ResourceNotFoundException("Lançamento não encontrado."));
    }

    private ContaBancaria buscarConta(Caso caso, UUID contaBancariaId) {
        return contaBancariaRepository.buscarPorId(contaBancariaId, caso.id)
                .orElseThrow(() -> new BusinessRuleException("Conta bancária não encontrada."));
    }

    private Categoria buscarCategoria(UUID categoriaId, TipoCategoria tipo) {
        Categoria categoria = categoriaRepository.buscarPorId(categoriaId, usuarioLogado())
                .orElseThrow(() -> new BusinessRuleException("Categoria não encontrada."));
        if (categoria.tipo != tipo) {
            throw new BusinessRuleException("A categoria não corresponde ao tipo do lançamento.");
        }
        return categoria;
    }

    private void validarData(LocalDate data, ContaBancaria conta) {
        if (data.isBefore(conta.dataSaldoInicial)) {
            throw new BusinessRuleException("A data do lançamento não pode ser anterior ao saldo inicial da conta.");
        }
    }

    private UUID usuarioLogado() {
        return UUID.fromString(jwt.getSubject());
    }
}
