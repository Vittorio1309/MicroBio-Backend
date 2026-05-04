package com.microbio.application.dto;

import com.microbio.application.model.OrcamentoStatus;
import java.util.List;

public record OrcamentoCreateDTO(
    Long pessoaId,
    Long servicoId,
    OrcamentoStatus status,
    String observacao,
    List<RespostaDTO> respostas
) {}
