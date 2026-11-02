package br.edu.unimater.zela.vara;

import br.edu.unimater.zela.shared.dto.OpcaoDto;
import br.edu.unimater.zela.shared.dto.Pagina;
import br.edu.unimater.zela.vara.dto.VaraCreateDto;
import br.edu.unimater.zela.vara.dto.VaraResponseDto;
import br.edu.unimater.zela.vara.dto.VaraUpdateDto;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.resteasy.reactive.ResponseStatus;

import java.util.List;
import java.util.UUID;

@Path("/varas")
@Authenticated
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Varas")
public class VaraResource {

    @Inject
    VaraService service;

    @GET
    public Pagina<VaraResponseDto> listar(@QueryParam("comarcaId") UUID comarcaId,
                                          @QueryParam("busca") String busca,
                                          @QueryParam("pagina") @DefaultValue("0") @Min(0) int pagina,
                                          @QueryParam("tamanho") @DefaultValue("10") @Min(1) @Max(100) int tamanho) {
        return service.listar(comarcaId, busca, pagina, tamanho);
    }

    @GET
    @Path("/opcoes")
    public List<OpcaoDto> listarOpcoes(@QueryParam("comarcaId") UUID comarcaId) {
        return service.listarOpcoes(comarcaId);
    }

    @GET
    @Path("/{id}")
    public VaraResponseDto buscar(@PathParam("id") UUID id) {
        return service.buscarPorId(id);
    }

    @POST
    @ResponseStatus(201)
    public VaraResponseDto criar(@Valid VaraCreateDto dto) {
        return service.criar(dto);
    }

    @PUT
    @Path("/{id}")
    public VaraResponseDto atualizar(@PathParam("id") UUID id, @Valid VaraUpdateDto dto) {
        return service.atualizar(id, dto);
    }

    @DELETE
    @Path("/{id}")
    public void excluir(@PathParam("id") UUID id) {
        service.excluir(id);
    }
}
