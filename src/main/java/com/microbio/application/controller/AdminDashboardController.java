package com.microbio.application.controller;

import com.microbio.application.dto.AdminDashboardStatsDTO;
import com.microbio.application.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/dashboard")
@Tag(name = "Admin - Dashboard", description = "Estatísticas consolidadas para o painel administrativo")
public class AdminDashboardController {

    private final DashboardService dashboardService;

    public AdminDashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/stats")
    @Operation(summary = "Retorna total de orçamentos, pendentes, finalizados e últimos 10 resultados")
    public ResponseEntity<AdminDashboardStatsDTO> getStats() {
        return ResponseEntity.ok(dashboardService.getAdminStats());
    }
}
