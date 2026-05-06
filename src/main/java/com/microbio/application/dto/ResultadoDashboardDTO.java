package com.microbio.application.dto;

import com.microbio.application.model.OrcamentoStatus;
import java.time.LocalDateTime;

/**
 * DTO para retornar resultado de exame com informações do orçamento na dashboard
 */
public record ResultadoDashboardDTO(
    Long orcamentoId,
    String nomeServico,
    String nomeCliente,
    LocalDateTime dataCriacao,
    OrcamentoStatus statusOrcamento,
    boolean temResultadoExame
) {}
