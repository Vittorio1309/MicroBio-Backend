package com.microbio.application.dto;

import com.microbio.application.model.ResultadoExameStatus;
import java.time.LocalDateTime;

public record ResultadoDashboardDTO(
    Long id,
    String nomeUsuario,
    String descricao,
    LocalDateTime dataEmissao,
    ResultadoExameStatus status
) {}
