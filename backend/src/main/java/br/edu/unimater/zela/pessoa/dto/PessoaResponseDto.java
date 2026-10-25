package br.edu.unimater.zela.pessoa.dto;

import br.edu.unimater.zela.pessoa.Pessoa;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record PessoaResponseDto(
        UUID id,
        String nome,
        String cpf,
        LocalDate dataNascimento,
        String endereco,
        String observacao,
        Instant criadoEm,
        Instant atualizadoEm
) {
    public static PessoaResponseDto from(Pessoa pessoa) {
        return new PessoaResponseDto(
                pessoa.id,
                pessoa.nome,
                pessoa.cpf,
                pessoa.dataNascimento,
                pessoa.endereco,
                pessoa.observacao,
                pessoa.criadoEm,
                pessoa.atualizadoEm
        );
    }
}