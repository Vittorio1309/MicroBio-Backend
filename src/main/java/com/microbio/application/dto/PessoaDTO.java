package com.microbio.application.dto;

public record PessoaDTO(
    Long id,
    String nome,
    String email,
    String telefone,
    String statusUltimoOrcamento
) {
}

