package com.microbio.application.controller;

import com.microbio.application.dto.OrcamentoCreateDTO;
import com.microbio.application.dto.OrcamentoDTO;
import com.microbio.application.service.OrcamentoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/orcamentos")
@Tag(name = "Admin - Orçamentos", description = "Criação de orçamentos sem validação de perguntas obrigatórias")
public class AdminOrcamentoController {

    private final OrcamentoService service;

    public AdminOrcamentoController(OrcamentoService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Criar orçamento (admin, sem validação de perguntas)")
    public ResponseEntity<OrcamentoDTO> create(@RequestBody OrcamentoCreateDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createByAdmin(dto));
    }
}
