package br.edu.unimater.zela.contabancaria;

import br.edu.unimater.zela.caso.Caso;
import br.edu.unimater.zela.caso.CasoRepository;
import br.edu.unimater.zela.contabancaria.dto.ContaBancariaCreateDto;
import br.edu.unimater.zela.contabancaria.dto.ContaBancariaResponseDto;
import br.edu.unimater.zela.contabancaria.dto.ContaBancariaUpdateDto;
import br.edu.unimater.zela.shared.dto.Pagina;
import br.edu.unimater.zela.shared.exception.BusinessRuleException;
import br.edu.unimater.zela.shared.exception.ConflictException;
import br.edu.unimater.zela.shared.exception.ResourceNotFoundException;
import br.edu.unimater.zela.shared.util.Texto;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.time.Instant;
import java.util.UUID;

@ApplicationScoped
public class ContaBancariaService {

    @Inject
    ContaBancariaRepository repository;

    @Inject
    CasoRepository casoRepository;

    @Inject
    JsonWebToken jwt;

    public Pagina<ContaBancariaResponseDto> listar(UUID casoId, int pagina, int tamanho) {
        Caso caso = buscarCaso(casoId);
        return repository.listar(caso.id, pagina, tamanho).map(ContaBancariaResponseDto::from);
    }

    public ContaBancariaResponseDto buscarPorId(UUID casoId, UUID id) {
        return ContaBancariaResponseDto.from(buscar(buscarCaso(casoId), id));
    }

    @Transactional
    public ContaBancariaResponseDto criar(UUID casoId, ContaBancariaCreateDto dto) {
        Caso caso = buscarCasoEditavel(casoId);
        String agencia = dto.agencia().trim().toUpperCase();
        String numero = dto.numero().trim().toUpperCase();
        validarContaDisponivel(caso.id, dto.banco(), agencia, numero, null);

        ContaBancaria conta = new ContaBancaria();
        conta.casoId = caso.id;
        conta.banco = dto.banco();
        conta.tipo = dto.tipo();
        conta.agencia = agencia;
        conta.numero = numero;
        conta.descricao = Texto.trimOuNulo(dto.descricao());
        conta.saldoInicial = dto.saldoInicial();
        conta.dataSaldoInicial = dto.dataSaldoInicial();

        repository.persistAndFlush(conta);
        return ContaBancariaResponseDto.from(conta);
    }

    @Transactional
    public ContaBancariaResponseDto atualizar(UUID casoId, UUID id, ContaBancariaUpdateDto dto) {
        Caso caso = buscarCasoEditavel(casoId);
        ContaBancaria conta = buscar(caso, id);
        String agencia = dto.agencia().trim().toUpperCase();
        String numero = dto.numero().trim().toUpperCase();
        validarContaDisponivel(caso.id, dto.banco(), agencia, numero, id);

        conta.banco = dto.banco();
        conta.tipo = dto.tipo();
        conta.agencia = agencia;
        conta.numero = numero;
        conta.descricao = Texto.trimOuNulo(dto.descricao());
        conta.saldoInicial = dto.saldoInicial();
        conta.dataSaldoInicial = dto.dataSaldoInicial();

        repository.flush();
        return ContaBancariaResponseDto.from(conta);
    }

    @Transactional
    public void excluir(UUID casoId, UUID id) {
        ContaBancaria conta = buscar(buscarCasoEditavel(casoId), id);
        conta.excluidoEm = Instant.now();
    }

    private Caso buscarCaso(UUID casoId) {
        return casoRepository.buscarPorId(casoId, usuarioLogado())
                .orElseThrow(() -> new ResourceNotFoundException("Caso não encontrado."));
    }

    private Caso buscarCasoEditavel(UUID casoId) {
        Caso caso = buscarCaso(casoId);
        if (caso.isEncerrado()) {
            throw new BusinessRuleException("Reabra o caso para alterar as contas bancárias.");
        }
        return caso;
    }

    private ContaBancaria buscar(Caso caso, UUID id) {
        return repository.buscarPorId(id, caso.id)
                .orElseThrow(() -> new ResourceNotFoundException("Conta bancária não encontrada."));
    }

    private void validarContaDisponivel(UUID casoId, Banco banco, String agencia, String numero, UUID idAtual) {
        repository.buscarPorNumero(casoId, banco, agencia, numero)
                .filter(outra -> !outra.id.equals(idAtual))
                .ifPresent(outra -> {
                    throw new ConflictException("Conta bancária já cadastrada neste caso.");
                });
    }

    private UUID usuarioLogado() {
        return UUID.fromString(jwt.getSubject());
    }
}
