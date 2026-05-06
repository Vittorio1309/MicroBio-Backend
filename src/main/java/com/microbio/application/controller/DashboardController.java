package com.microbio.application.controller;

import com.microbio.application.dto.EstatisticasDTO;
import com.microbio.application.dto.ResultadoDashboardDTO;
import com.microbio.application.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@Tag(name = "Dashboard", description = "Endpoints para dashboard em tempo real")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/estatisticas")
    @Operation(summary = "Retorna estatísticas gerais de orçamentos")
    public ResponseEntity<EstatisticasDTO> getEstatisticas() {
        return ResponseEntity.ok(dashboardService.getEstatisticas());
    }

    @GetMapping("/resultados-recentes")
    @Operation(summary = "Retorna os últimos 10 resultados de exames com informações dos orçamentos")
    public ResponseEntity<List<ResultadoDashboardDTO>> getResultadosRecentes() {
        return ResponseEntity.ok(dashboardService.getResultadosRecentes());
    }
}

