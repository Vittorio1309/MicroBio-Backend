package com.microbio.application.dto;

public record PessoaUpdate(
    String nome,
    String email,
    String senha,
    String telefone
) {
}

