package com.microbio.application.dto;

import com.microbio.application.model.OrcamentoStatus;

public record OrcamentoUpdateDTO(
    OrcamentoStatus status,
    String observacao,
    Long pessoaId,
    Long servicoId
) {}
