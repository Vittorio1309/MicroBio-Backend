package com.microbio.application.dto;

import com.microbio.application.model.OrcamentoStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrcamentoDTO(
    Long id,
    LocalDateTime dataCriacao,
    LocalDateTime dataMovimentacao,
    OrcamentoStatus status,
    String observacao,
    BigDecimal valorTotal,
    Long pessoaId,
    String pessoaNome,
    Long servicoId,
    String servicoNome,
    Long responsavelId,
    String responsavelNome,
    LocalDateTime dataAtribuicao,
    List<RespostaDTO> respostas
) {}
