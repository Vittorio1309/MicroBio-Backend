package com.microbio.application.dto;

import com.microbio.application.model.ResultadoExameStatus;

public record ResultadoExameUpdateDTO(
    String descricao,
    String laudo,
    String arquivoUrl,
    ResultadoExameStatus status
) {}
