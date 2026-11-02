package br.edu.unimater.zela.comarca;

import br.edu.unimater.zela.comarca.dto.ComarcaCreateDto;
import br.edu.unimater.zela.comarca.dto.ComarcaResponseDto;
import br.edu.unimater.zela.comarca.dto.ComarcaUpdateDto;
import br.edu.unimater.zela.shared.dto.OpcaoDto;
import br.edu.unimater.zela.shared.dto.Pagina;
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

@Path("/comarcas")
@Authenticated
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Comarcas")
public class ComarcaResource {

    @Inject
    ComarcaService service;

    @GET
    public Pagina<ComarcaResponseDto> listar(@QueryParam("busca") String busca,
                                             @QueryParam("pagina") @DefaultValue("0") @Min(0) int pagina,
                                             @QueryParam("tamanho") @DefaultValue("10") @Min(1) @Max(100) int tamanho) {
        return service.listar(busca, pagina, tamanho);
    }

    @GET
    @Path("/opcoes")
    public List<OpcaoDto> listarOpcoes() {
        return service.listarOpcoes();
    }

    @GET
    @Path("/{id}")
    public ComarcaResponseDto buscar(@PathParam("id") UUID id) {
        return service.buscarPorId(id);
    }

    @POST
    @ResponseStatus(201)
    public ComarcaResponseDto criar(@Valid ComarcaCreateDto dto) {
        return service.criar(dto);
    }

    @PUT
    @Path("/{id}")
    public ComarcaResponseDto atualizar(@PathParam("id") UUID id, @Valid ComarcaUpdateDto dto) {
        return service.atualizar(id, dto);
    }

    @DELETE
    @Path("/{id}")
    public void excluir(@PathParam("id") UUID id) {
        service.excluir(id);
    }
}
