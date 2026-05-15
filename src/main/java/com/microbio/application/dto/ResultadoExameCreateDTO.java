package com.microbio.application.dto;

public record ResultadoExameCreateDTO(
    String descricao,
    String laudo,
    String arquivoUrl,
    Long pessoaId,
    Long servicoId
) {}
