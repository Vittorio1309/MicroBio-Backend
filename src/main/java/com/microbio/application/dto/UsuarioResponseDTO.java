package com.microbio.application.dto;

public record UsuarioResponseDTO(
    Long id,
    String username,
    String role,
    Long pessoaId,
    String nomePessoa
) {}
