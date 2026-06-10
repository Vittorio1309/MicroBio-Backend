package com.microbio.application.dto;

public record ResultadoExameCreateDTO(
    Long usuarioId,
    String descricao,
    String laudo,
    String arquivoUrl
) {}
