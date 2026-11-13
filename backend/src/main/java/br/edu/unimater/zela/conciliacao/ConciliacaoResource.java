package br.edu.unimater.zela.conciliacao;

import br.edu.unimater.zela.conciliacao.dto.ConciliacaoAutomaticaResultadoDto;
import br.edu.unimater.zela.conciliacao.dto.ConciliacaoResumoDto;
import br.edu.unimater.zela.conciliacao.dto.MovimentacaoConciliacaoResponseDto;
import br.edu.unimater.zela.importacaoextrato.SituacaoMovimentacao;
import br.edu.unimater.zela.shared.dto.Pagina;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.time.LocalDate;
import java.util.UUID;

@Path("/casos/{casoId}/conciliacao")
@Authenticated
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Conciliação")
public class ConciliacaoResource {

    @Inject
    ConciliacaoService service;

    @GET
    @Path("/movimentacoes")
    public Pagina<MovimentacaoConciliacaoResponseDto> listar(@PathParam("casoId") UUID casoId,
                                                             @QueryParam("contaBancariaId") UUID contaBancariaId,
                                                             @QueryParam("dataInicio") LocalDate dataInicio,
                                                             @QueryParam("dataFim") LocalDate dataFim,
                                                             @QueryParam("situacao") SituacaoMovimentacao situacao,
                                                             @QueryParam("pagina") @DefaultValue("0") @Min(0) int pagina,
                                                             @QueryParam("tamanho") @DefaultValue("10") @Min(1) @Max(100) int tamanho) {
        return service.listar(casoId, contaBancariaId, dataInicio, dataFim, situacao, pagina, tamanho);
    }

    @GET
    @Path("/resumo")
    public ConciliacaoResumoDto resumo(@PathParam("casoId") UUID casoId,
                                       @QueryParam("contaBancariaId") UUID contaBancariaId,
                                       @QueryParam("dataInicio") LocalDate dataInicio,
                                       @QueryParam("dataFim") LocalDate dataFim) {
        return service.resumo(casoId, contaBancariaId, dataInicio, dataFim);
    }

    @POST
    @Path("/automatica")
    public ConciliacaoAutomaticaResultadoDto conciliarAutomaticamente(@PathParam("casoId") UUID casoId,
                                                                      @QueryParam("contaBancariaId") UUID contaBancariaId) {
        return service.conciliarAutomaticamente(casoId, contaBancariaId);
    }
}
