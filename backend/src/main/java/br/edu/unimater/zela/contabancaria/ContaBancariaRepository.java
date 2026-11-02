package br.edu.unimater.zela.contabancaria;

import br.edu.unimater.zela.shared.dto.Pagina;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class ContaBancariaRepository implements PanacheRepositoryBase<ContaBancaria, UUID> {

    public Pagina<ContaBancaria> listar(UUID casoId, int pagina, int tamanho) {
        return Pagina.de(find("casoId = ?1 and excluidoEm is null order by banco, agencia, numero", casoId),
                pagina, tamanho);
    }

    public Optional<ContaBancaria> buscarPorId(UUID id, UUID casoId) {
        return find("id = ?1 and casoId = ?2 and excluidoEm is null", id, casoId).firstResultOptional();
    }

    public Optional<ContaBancaria> buscarPorNumero(UUID casoId, Banco banco, String agencia, String numero) {
        return find("casoId = ?1 and banco = ?2 and agencia = ?3 and numero = ?4 and excluidoEm is null",
                casoId, banco, agencia, numero).firstResultOptional();
    }

    public boolean existePorCaso(UUID casoId) {
        return count("casoId = ?1 and excluidoEm is null", casoId) > 0;
    }
}
