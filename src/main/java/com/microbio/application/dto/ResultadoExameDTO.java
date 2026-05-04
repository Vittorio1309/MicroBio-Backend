package com.microbio.application.dto;

import java.time.LocalDateTime;

public record ResultadoExameDTO(
    Long id,
    String descricao,
    LocalDateTime dataEmissao,
    String laudo,
    String arquivoUrl,
    Long orcamentoId
) {}
