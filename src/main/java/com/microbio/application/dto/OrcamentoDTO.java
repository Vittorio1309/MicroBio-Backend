package com.microbio.application.dto;

import java.time.LocalDateTime;

public record OrcamentoDTO(
    Long id,
    LocalDateTime dataCriacao,
    OrcamentoStatus status,
    String observacao,
    Long pessoaId,
    Long servicoId
) {
}