package br.edu.unimater.zela.pessoa;

import br.edu.unimater.zela.caso.CasoRepository;
import br.edu.unimater.zela.pessoa.dto.PessoaCreateDto;
import br.edu.unimater.zela.pessoa.dto.PessoaResponseDto;
import br.edu.unimater.zela.pessoa.dto.PessoaUpdateDto;
import br.edu.unimater.zela.shared.dto.OpcaoDto;
import br.edu.unimater.zela.shared.dto.Pagina;
import br.edu.unimater.zela.shared.exception.BusinessRuleException;
import br.edu.unimater.zela.shared.exception.ConflictException;
import br.edu.unimater.zela.shared.exception.ResourceNotFoundException;
import br.edu.unimater.zela.shared.util.Texto;
import br.edu.unimater.zela.usuario.UsuarioService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class PessoaService {

    @Inject
    PessoaRepository repository;

    @Inject
    CasoRepository casoRepository;

    @Inject
    UsuarioService usuarioService;

    @Inject
    JsonWebToken jwt;

    public Pagina<PessoaResponseDto> listar(String busca, int pagina, int tamanho) {
        return repository.listar(usuarioLogado(), busca, pagina, tamanho).map(PessoaResponseDto::from);
    }

    public List<OpcaoDto> listarOpcoes() {
        return repository.listarOpcoes(usuarioLogado()).stream()
                .map(pessoa -> new OpcaoDto(pessoa.id, pessoa.nome))
                .toList();
    }

    public PessoaResponseDto buscarPorId(UUID id) {
        return PessoaResponseDto.from(buscar(id));
    }

    @Transactional
    public PessoaResponseDto criar(PessoaCreateDto dto) {
        UUID usuarioId = usuarioService.garantirUsuarioLogado();;
        String cpf = Texto.apenasDigitos(dto.cpf());
        validarCpfDisponivel(cpf, usuarioId, null);

        Pessoa pessoa = new Pessoa();
        pessoa.usuarioId = usuarioId;
        pessoa.nome = dto.nome().trim();
        pessoa.cpf = cpf;
        pessoa.dataNascimento = dto.dataNascimento();
        pessoa.endereco = Texto.trimOuNulo(dto.endereco());
        pessoa.observacao = Texto.trimOuNulo(dto.observacao());

        repository.persistAndFlush(pessoa);
        return PessoaResponseDto.from(pessoa);
    }

    @Transactional
    public PessoaResponseDto atualizar(UUID id, PessoaUpdateDto dto) {
        Pessoa pessoa = buscar(id);
        String cpf = Texto.apenasDigitos(dto.cpf());
        validarCpfDisponivel(cpf, pessoa.usuarioId, id);

        pessoa.nome = dto.nome().trim();
        pessoa.cpf = cpf;
        pessoa.dataNascimento = dto.dataNascimento();
        pessoa.endereco = Texto.trimOuNulo(dto.endereco());
        pessoa.observacao = Texto.trimOuNulo(dto.observacao());

        repository.flush();
        return PessoaResponseDto.from(pessoa);
    }

    @Transactional
    public void excluir(UUID id) {
        UUID usuarioId = usuarioLogado();
        if (casoRepository.existePorPessoa(id, usuarioId)) {
            throw new BusinessRuleException("Esta pessoa está vinculada a um caso e não pode ser excluída.");
        }
        if (!repository.excluir(id, usuarioId)) {
            throw new ResourceNotFoundException("Pessoa não encontrada.");
        }
    }

    private Pessoa buscar(UUID id) {
        return repository.buscarPorId(id, usuarioLogado())
                .orElseThrow(() -> new ResourceNotFoundException("Pessoa não encontrada."));
    }

    private void validarCpfDisponivel(String cpf, UUID usuarioId, UUID idAtual) {
        if (cpf == null) {
            return;
        }
        repository.buscarPorCpf(cpf, usuarioId)
                .filter(outra -> !outra.id.equals(idAtual))
                .ifPresent(outra -> {
                    throw new ConflictException("CPF já cadastrado.");
                });
    }

    private UUID usuarioLogado() {
        return UUID.fromString(jwt.getSubject());
    }
    
}