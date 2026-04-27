package com.microbio.application.dto;

public record OrcamentoCreateDTO(
    OrcamentoStatus status,
    String observacao,
    Long pessoaId,
    Long servicoId
) {
}