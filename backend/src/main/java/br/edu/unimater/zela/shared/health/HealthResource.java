package br.edu.unimater.zela.shared.health;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import javax.sql.DataSource;
import java.sql.SQLException;

@Path("/health")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Sistema")
public class HealthResource {

    public record HealthResponse(String status, String db) {}

    @Inject
    DataSource dataSource;

    @GET
    @Operation(summary = "Verifica se a API e o banco estão no ar")
    @APIResponse(responseCode = "200", description = "API e banco disponíveis", content = @Content(schema = @Schema(implementation = HealthResponse.class)))
    @APIResponse(responseCode = "503", description = "Banco indisponível", content = @Content(schema = @Schema(implementation = HealthResponse.class)))
    public Response health() {
        try (var conn = dataSource.getConnection();
             var stmt = conn.createStatement();
             var rs = stmt.executeQuery("SELECT 1")) {
            rs.next();
            return Response.ok(new HealthResponse("UP", "UP")).build();
        } catch (SQLException e) {
            return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                    .entity(new HealthResponse("DOWN", "DOWN"))
                    .build();
        }
    }
}