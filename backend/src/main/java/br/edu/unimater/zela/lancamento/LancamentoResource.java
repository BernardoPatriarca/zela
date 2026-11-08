package br.edu.unimater.zela.lancamento;

import br.edu.unimater.zela.categoria.TipoCategoria;
import br.edu.unimater.zela.lancamento.dto.LancamentoCreateDto;
import br.edu.unimater.zela.lancamento.dto.LancamentoExclusaoDto;
import br.edu.unimater.zela.lancamento.dto.LancamentoResponseDto;
import br.edu.unimater.zela.lancamento.dto.LancamentoUpdateDto;
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

import java.time.LocalDate;
import java.util.UUID;

@Path("/casos/{casoId}/lancamentos")
@Authenticated
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Lançamentos")
public class LancamentoResource {

    @Inject
    LancamentoService service;

    @GET
    public Pagina<LancamentoResponseDto> listar(@PathParam("casoId") UUID casoId,
                                                @QueryParam("dataInicio") LocalDate dataInicio,
                                                @QueryParam("dataFim") LocalDate dataFim,
                                                @QueryParam("contaBancariaId") UUID contaBancariaId,
                                                @QueryParam("tipo") TipoCategoria tipo,
                                                @QueryParam("categoriaId") UUID categoriaId,
                                                @QueryParam("situacao") SituacaoLancamento situacao,
                                                @QueryParam("pagina") @DefaultValue("0") @Min(0) int pagina,
                                                @QueryParam("tamanho") @DefaultValue("10") @Min(1) @Max(100) int tamanho) {
        return service.listar(casoId, dataInicio, dataFim, contaBancariaId, tipo, categoriaId, situacao,
                pagina, tamanho);
    }

    @GET
    @Path("/{id}")
    public LancamentoResponseDto buscar(@PathParam("casoId") UUID casoId, @PathParam("id") UUID id) {
        return service.buscarPorId(casoId, id);
    }

    @POST
    @ResponseStatus(201)
    public LancamentoResponseDto criar(@PathParam("casoId") UUID casoId, @Valid LancamentoCreateDto dto) {
        return service.criar(casoId, dto);
    }

    @PUT
    @Path("/{id}")
    public LancamentoResponseDto atualizar(@PathParam("casoId") UUID casoId, @PathParam("id") UUID id,
                                           @Valid LancamentoUpdateDto dto) {
        return service.atualizar(casoId, id, dto);
    }

    @DELETE
    @Path("/{id}")
    public void excluir(@PathParam("casoId") UUID casoId, @PathParam("id") UUID id,
                        @Valid LancamentoExclusaoDto dto) {
        service.excluir(casoId, id, dto);
    }
}
