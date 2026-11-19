package br.edu.unimater.zela.prestacaocontas;

import br.edu.unimater.zela.caso.SituacaoPeriodo;
import br.edu.unimater.zela.prestacaocontas.dto.ArquivoRelatorioDto;
import br.edu.unimater.zela.prestacaocontas.dto.EntregaRequestDto;
import br.edu.unimater.zela.prestacaocontas.dto.JustificativaRequestDto;
import br.edu.unimater.zela.prestacaocontas.dto.PeriodoAtualizacaoDto;
import br.edu.unimater.zela.prestacaocontas.dto.PrestacaoContasDetalheDto;
import br.edu.unimater.zela.prestacaocontas.dto.PrestacaoContasResumoDto;
import br.edu.unimater.zela.prestacaocontas.dto.RelatorioResponseDto;
import br.edu.unimater.zela.prestacaocontas.dto.SaldoInicialRequestDto;
import br.edu.unimater.zela.shared.dto.Pagina;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.ws.rs.Consumes;
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
import org.jboss.resteasy.reactive.RestResponse;

import java.util.List;
import java.util.UUID;

@Path("/prestacoes-contas")
@Authenticated
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Prestação de contas")
public class PrestacaoContasResource {

    @Inject
    PrestacaoContasService service;

    @Inject
    RelatorioService relatorioService;

    @GET
    public Pagina<PrestacaoContasResumoDto> listar(@QueryParam("casoId") UUID casoId,
                                                   @QueryParam("situacao") SituacaoPeriodo situacao,
                                                   @QueryParam("pagina") @DefaultValue("0") @Min(0) int pagina,
                                                   @QueryParam("tamanho") @DefaultValue("10") @Min(1) @Max(100) int tamanho) {
        return service.listar(casoId, situacao, pagina, tamanho);
    }

    @GET
    @Path("/{id}")
    public PrestacaoContasDetalheDto buscar(@PathParam("id") UUID id) {
        return service.buscarPorId(id);
    }

    @PUT
    @Path("/{id}")
    public PrestacaoContasDetalheDto atualizar(@PathParam("id") UUID id, @Valid PeriodoAtualizacaoDto dto) {
        return service.atualizar(id, dto);
    }

    @PUT
    @Path("/{id}/saldos-iniciais")
    public void informarSaldosIniciais(@PathParam("id") UUID id, @Valid List<SaldoInicialRequestDto> saldos) {
        service.informarSaldosIniciais(id, saldos);
    }

    @PUT
    @Path("/{id}/justificativas")
    public void salvarJustificativa(@PathParam("id") UUID id, @Valid JustificativaRequestDto dto) {
        service.salvarJustificativa(id, dto);
    }

    @POST
    @Path("/{id}/recebimento-curador")
    public PrestacaoContasDetalheDto receberDoCurador(@PathParam("id") UUID id) {
        return service.receberDoCurador(id);
    }

    @POST
    @Path("/{id}/revisao")
    public PrestacaoContasDetalheDto voltarParaRevisao(@PathParam("id") UUID id) {
        return service.voltarParaRevisao(id);
    }

    @POST
    @Path("/{id}/entrega")
    public PrestacaoContasDetalheDto entregar(@PathParam("id") UUID id, @Valid EntregaRequestDto dto) {
        return service.entregar(id, dto);
    }

    @GET
    @Path("/{id}/relatorios")
    public List<RelatorioResponseDto> listarRelatorios(@PathParam("id") UUID id) {
        return relatorioService.listar(id);
    }

    @POST
    @Path("/{id}/relatorios")
    @ResponseStatus(201)
    public RelatorioResponseDto gerarRelatorio(@PathParam("id") UUID id) {
        return relatorioService.gerar(id);
    }

    @GET
    @Path("/{id}/relatorios/{relatorioId}/arquivo")
    @Produces("application/pdf")
    public RestResponse<byte[]> baixarRelatorio(@PathParam("id") UUID id,
                                                @PathParam("relatorioId") UUID relatorioId) {
        ArquivoRelatorioDto arquivo = relatorioService.baixar(id, relatorioId);
        return RestResponse.ResponseBuilder.ok(arquivo.conteudo())
                .header("Content-Disposition", "attachment; filename=\"" + arquivo.nome() + "\"")
                .build();
    }
}
