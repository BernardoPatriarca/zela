package br.edu.unimater.zela.usuario;

import br.edu.unimater.zela.usuario.dto.UsuarioCreateDto;
import br.edu.unimater.zela.usuario.dto.UsuarioResponseDto;
import br.edu.unimater.zela.usuario.dto.UsuarioUpdateDto;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.resteasy.reactive.ResponseStatus;

import java.util.List;
import java.util.UUID;

@Path("/usuarios")
@Authenticated
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Usuários")
public class UsuarioResource {

    @Inject
    UsuarioService service;

    @Inject
    JsonWebToken jwt;

    @GET
    public List<UsuarioResponseDto> listar() {
        return service.listar();
    }

    @GET
    @Path("/me")
    public UsuarioResponseDto me() {
        return service.buscarPorId(UUID.fromString(jwt.getSubject()));
    }

    @GET
    @Path("/{id}")
    public UsuarioResponseDto buscar(@PathParam("id") UUID id) {
        return service.buscarPorId(id);
    }

    @POST
    @ResponseStatus(201)
    public UsuarioResponseDto criar(@Valid UsuarioCreateDto dto) {
        return service.criar(dto);
    }

    @PUT
    @Path("/{id}")
    public UsuarioResponseDto atualizar(@Valid @PathParam("id") UUID id, UsuarioUpdateDto dto) {
        return service.atualizar(id, dto);
    }

    @DELETE
    @Path("/{id}")
    public void excluir(@PathParam("id") UUID id) {
        service.excluir(id);
    }
}