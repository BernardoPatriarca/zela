package br.edu.unimater.zela.contabancaria;

import br.edu.unimater.zela.contabancaria.dto.ContaBancariaCreateDto;
import br.edu.unimater.zela.contabancaria.dto.ContaBancariaResponseDto;
import br.edu.unimater.zela.contabancaria.dto.ContaBancariaUpdateDto;
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

import java.util.UUID;

@Path("/casos/{casoId}/contas-bancarias")
@Authenticated
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Contas bancárias")
public class ContaBancariaResource {

    @Inject
    ContaBancariaService service;

    @GET
    public Pagina<ContaBancariaResponseDto> listar(@PathParam("casoId") UUID casoId,
                                                   @QueryParam("pagina") @DefaultValue("0") @Min(0) int pagina,
                                                   @QueryParam("tamanho") @DefaultValue("10") @Min(1) @Max(100) int tamanho) {
        return service.listar(casoId, pagina, tamanho);
    }

    @GET
    @Path("/{id}")
    public ContaBancariaResponseDto buscar(@PathParam("casoId") UUID casoId, @PathParam("id") UUID id) {
        return service.buscarPorId(casoId, id);
    }

    @POST
    @ResponseStatus(201)
    public ContaBancariaResponseDto criar(@PathParam("casoId") UUID casoId, @Valid ContaBancariaCreateDto dto) {
        return service.criar(casoId, dto);
    }

    @PUT
    @Path("/{id}")
    public ContaBancariaResponseDto atualizar(@PathParam("casoId") UUID casoId, @PathParam("id") UUID id,
                                              @Valid ContaBancariaUpdateDto dto) {
        return service.atualizar(casoId, id, dto);
    }

    @DELETE
    @Path("/{id}")
    public void excluir(@PathParam("casoId") UUID casoId, @PathParam("id") UUID id) {
        service.excluir(casoId, id);
    }
}
