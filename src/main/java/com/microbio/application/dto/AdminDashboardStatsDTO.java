package com.microbio.application.dto;

import java.util.List;

public record AdminDashboardStatsDTO(
    Long totalAnalises,
    Long totalEmAndamento,
    Long totalFinalizadas,
    List<ResultadoDashboardDTO> resultadosRecentes
) {}
