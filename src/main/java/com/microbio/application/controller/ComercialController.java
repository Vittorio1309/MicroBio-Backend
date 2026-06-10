package com.microbio.application.controller;

import com.microbio.application.dto.ComercialAnaliseDTO;
import com.microbio.application.service.ComercialService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api/admin/comercial")
@PreAuthorize("hasAnyRole('ADMIN', 'ADMIN_MASTER')")
@Tag(name = "Admin - Comercial", description = "Endpoints de análise comercial e conversão")
public class ComercialController {

    private final ComercialService service;

    public ComercialController(ComercialService service) {
        this.service = service;
    }

    @GetMapping("/analise")
    @Operation(summary = "Obter indicadores de análise comercial dos orçamentos")
    public ResponseEntity<ComercialAnaliseDTO> obterAnalise(
            @RequestParam(required = false) Long responsavelId,
            org.springframework.security.core.Authentication authentication) {
        
        String username = authentication.getName();
        String role = authentication.getAuthorities().stream()
                .map(g -> g.getAuthority())
                .findFirst()
                .orElse("ROLE_USER");
        if (role.startsWith("ROLE_")) {
            role = role.substring(5);
        }

        return ResponseEntity.ok(service.obterAnaliseComercial(username, role, responsavelId));
    }

    @GetMapping("/ranking")
    @Operation(summary = "Obter ranking de conversões por responsável comercial")
    public ResponseEntity<java.util.List<com.microbio.application.dto.ResponsavelRankingDTO>> obterRanking(
            @RequestParam(defaultValue = "30dias") String periodo,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate inicio,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate fim) {
        return ResponseEntity.ok(service.obterRankingResponsaveis(periodo, inicio, fim));
    }
}
