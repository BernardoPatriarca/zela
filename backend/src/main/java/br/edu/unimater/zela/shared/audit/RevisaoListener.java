package br.edu.unimater.zela.shared.audit;

import io.quarkus.arc.Arc;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.hibernate.envers.RevisionListener;

import java.util.UUID;

public class RevisaoListener implements RevisionListener {

    @Override
    public void newRevision(Object revisionEntity) {
        Revisao revisao = (Revisao) revisionEntity;
        revisao.usuarioId = usuarioAutenticado();
    }

    private UUID usuarioAutenticado() {
        var container = Arc.container();
        if (!container.requestContext().isActive()) {
            return null;
        }
        String sub = container.instance(JsonWebToken.class).get().getSubject();
        return sub == null ? null : UUID.fromString(sub);
    }
}