package com.microbio.application.dto;

import com.microbio.application.model.ResultadoExameStatus;
import java.time.LocalDateTime;

public record ResultadoExameDTO(
    Long id,
    String descricao,
    LocalDateTime dataEmissao,
    String laudo,
    String arquivoUrl,
    Long usuarioId,
    String username,
    ResultadoExameStatus status
) {}
