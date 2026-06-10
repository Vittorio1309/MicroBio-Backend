package com.microbio.application.dto;

public record UsuarioCreateDTO(
    String username,
    String password,
    String role,
    Long pessoaId
) {
}
