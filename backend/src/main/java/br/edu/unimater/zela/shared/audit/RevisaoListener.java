package br.edu.unimater.zela.shared.audit;

import org.hibernate.envers.RevisionListener;

public class RevisaoListener implements RevisionListener {

    @Override
    public void newRevision(Object revisionEntity) {
        Revisao revisao = (Revisao) revisionEntity;
        // TODO (etapa de segurança): preencher com o claim "sub" do JWT do Supabase.
    }
}