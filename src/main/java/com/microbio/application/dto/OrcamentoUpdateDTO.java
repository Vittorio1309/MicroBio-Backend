package com.microbio.application.dto;

public record OrcamentoUpdateDTO(
    OrcamentoStatus status,
    String observacao,
    Long pessoaId,
    Long servicoId
) {
}