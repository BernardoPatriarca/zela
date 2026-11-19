package br.edu.unimater.zela.prestacaocontas;

import br.edu.unimater.zela.prestacaocontas.dto.DocumentoPrestacaoDto;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import jakarta.enterprise.context.ApplicationScoped;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;

@ApplicationScoped
public class RelatorioPdf {

    @CheckedTemplate
    static class Templates {
        static native TemplateInstance prestacao(DocumentoPrestacaoDto documento);
    }

    public byte[] gerar(DocumentoPrestacaoDto documento) {
        String html = Templates.prestacao(documento).render();

        try (ByteArrayOutputStream saida = new ByteArrayOutputStream()) {
            PdfRendererBuilder renderizador = new PdfRendererBuilder();
            renderizador.useFont(() -> RelatorioPdf.class.getResourceAsStream("/fonts/LiberationSans-Regular.ttf"),
                    "Liberation Sans", 400, PdfRendererBuilder.FontStyle.NORMAL, true);
            renderizador.useFont(() -> RelatorioPdf.class.getResourceAsStream("/fonts/LiberationSans-Bold.ttf"),
                    "Liberation Sans", 700, PdfRendererBuilder.FontStyle.NORMAL, true);
            renderizador.withHtmlContent(html, null);
            renderizador.toStream(saida);
            renderizador.run();
            return saida.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
