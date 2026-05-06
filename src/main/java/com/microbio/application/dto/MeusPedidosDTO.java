package com.microbio.application.dto;

import com.microbio.application.model.OrcamentoStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO para retornar orçamentos do usuário autenticado
 */
public record MeusPedidosDTO(
    Long id,
    LocalDateTime dataCriacao,
    OrcamentoStatus status,
    String nomeServico,
    BigDecimal valorTotal,
    String observacao
) {}

