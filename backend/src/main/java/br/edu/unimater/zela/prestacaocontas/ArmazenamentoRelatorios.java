package br.edu.unimater.zela.prestacaocontas;

import br.edu.unimater.zela.shared.exception.ResourceNotFoundException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@ApplicationScoped
public class ArmazenamentoRelatorios {

    @Inject
    S3Client s3;

    @ConfigProperty(name = "storage.relatorios.bucket")
    String bucket;

    public void enviar(String chave, byte[] conteudo) {
        s3.putObject(PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(chave)
                        .contentType("application/pdf")
                        .build(),
                RequestBody.fromBytes(conteudo));
    }

    public byte[] baixar(String chave) {
        try {
            return s3.getObjectAsBytes(GetObjectRequest.builder().bucket(bucket).key(chave).build()).asByteArray();
        } catch (NoSuchKeyException e) {
            throw new ResourceNotFoundException("Arquivo do relatório não encontrado.");
        }
    }
}
