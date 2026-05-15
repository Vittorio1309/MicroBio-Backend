package com.microbio.application.dto;

public record PessoaCreateDTO(
    String nome,
    String email,
    String senha,
    String telefone
) {
}

