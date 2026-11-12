package br.edu.unimater.zela.importacaoextrato;

import br.edu.unimater.zela.importacaoextrato.dto.ImportacaoExtratoExclusaoDto;
import br.edu.unimater.zela.importacaoextrato.dto.ImportacaoExtratoResponseDto;
import br.edu.unimater.zela.importacaoextrato.dto.ImportacaoExtratoResultadoDto;
import br.edu.unimater.zela.shared.dto.Pagina;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.resteasy.reactive.ResponseStatus;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.UUID;

@Path("/casos/{casoId}/importacoes-extrato")
@Authenticated
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Importação de extratos")
public class ImportacaoExtratoResource {

    @Inject
    ImportacaoExtratoService service;

    @GET
    public Pagina<ImportacaoExtratoResponseDto> listar(@PathParam("casoId") UUID casoId,
                                                       @QueryParam("contaBancariaId") UUID contaBancariaId,
                                                       @QueryParam("dataInicio") LocalDate dataInicio,
                                                       @QueryParam("dataFim") LocalDate dataFim,
                                                       @QueryParam("pagina") @DefaultValue("0") @Min(0) int pagina,
                                                       @QueryParam("tamanho") @DefaultValue("10") @Min(1) @Max(100) int tamanho) {
        return service.listar(casoId, contaBancariaId, dataInicio, dataFim, pagina, tamanho);
    }

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @ResponseStatus(201)
    public ImportacaoExtratoResultadoDto importar(@PathParam("casoId") UUID casoId,
                                                  @RestForm("contaBancariaId")
                                                  @NotNull(message = "A conta bancária é obrigatória.")
                                                  UUID contaBancariaId,
                                                  @RestForm("arquivo")
                                                  @NotNull(message = "O arquivo é obrigatório.")
                                                  FileUpload arquivo) {
        try {
            return service.importar(casoId, contaBancariaId, arquivo.fileName(),
                    Files.readAllBytes(arquivo.uploadedFile()));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @DELETE
    @Path("/{id}")
    public void excluir(@PathParam("casoId") UUID casoId, @PathParam("id") UUID id,
                        @Valid ImportacaoExtratoExclusaoDto dto) {
        service.excluir(casoId, id, dto);
    }
}
