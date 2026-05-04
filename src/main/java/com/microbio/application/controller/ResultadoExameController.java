package com.microbio.application.controller;

import com.microbio.application.dto.ResultadoExameCreateDTO;
import com.microbio.application.dto.ResultadoExameDTO;
import com.microbio.application.service.ResultadoExameService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/resultados")
@Tag(name = "Resultados de Exame", description = "Consulta e gerenciamento de resultados de exames")
public class ResultadoExameController {

    private final ResultadoExameService service;

    public ResultadoExameController(ResultadoExameService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar todos os resultados (Admin)")
    public ResponseEntity<List<ResultadoExameDTO>> getAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar resultado por ID")
    public ResponseEntity<ResultadoExameDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @GetMapping("/orcamento/{orcamentoId}")
    @Operation(summary = "Listar resultados de um orçamento")
    public ResponseEntity<List<ResultadoExameDTO>> getByOrcamento(@PathVariable Long orcamentoId) {
        return ResponseEntity.ok(service.findByOrcamento(orcamentoId));
    }

    @GetMapping("/pessoa/{pessoaId}")
    @Operation(summary = "Listar resultados de exames de uma pessoa (cliente)")
    public ResponseEntity<List<ResultadoExameDTO>> getByPessoa(@PathVariable Long pessoaId) {
        return ResponseEntity.ok(service.findByPessoa(pessoaId));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cadastrar resultado de exame (Admin)")
    public ResponseEntity<ResultadoExameDTO> create(@RequestBody ResultadoExameCreateDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Remover resultado de exame (Admin)")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
