package com.microbio.application.dto;

/**
 * DTO para retornar estatísticas gerais do dashboard
 */
public record EstatisticasDTO(
    Long totalOrcamentos,
    Long totalPendente,
    Long totalFinalizado
) {}
