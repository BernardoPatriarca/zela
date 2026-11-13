package br.edu.unimater.zela.contabancaria;

import br.edu.unimater.zela.shared.dto.Pagina;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class ContaBancariaRepository implements PanacheRepositoryBase<ContaBancaria, UUID> {

    public Pagina<ContaBancaria> listar(UUID casoId, int pagina, int tamanho) {
        List<ContaBancaria> contas = find("""
                from ContaBancaria c join fetch c.banco b
                where c.casoId = ?1 and c.excluidoEm is null
                order by b.nome, c.agencia, c.numero
                """, casoId)
                .page(pagina, tamanho)
                .list();
        long total = count("casoId = ?1 and excluidoEm is null", casoId);
        return new Pagina<>(contas, total, pagina, tamanho);
    }

    public List<ContaBancaria> listarPorCaso(UUID casoId) {
        return list("casoId = ?1 and excluidoEm is null", casoId);
    }

    public Optional<ContaBancaria> buscarPorId(UUID id, UUID casoId) {
        return find("""
                from ContaBancaria c join fetch c.banco
                where c.id = ?1 and c.casoId = ?2 and c.excluidoEm is null
                """, id, casoId).firstResultOptional();
    }

    public Optional<ContaBancaria> buscarPorNumero(UUID casoId, UUID bancoId, String agencia, String numero) {
        return find("casoId = ?1 and banco.id = ?2 and agencia = ?3 and numero = ?4 and excluidoEm is null",
                casoId, bancoId, agencia, numero).firstResultOptional();
    }

    public boolean existePorBanco(UUID bancoId) {
        return count("banco.id = ?1 and excluidoEm is null", bancoId) > 0;
    }

    public boolean existePorCaso(UUID casoId) {
        return count("casoId = ?1 and excluidoEm is null", casoId) > 0;
    }
}
