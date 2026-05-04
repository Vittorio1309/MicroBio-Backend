package com.microbio.application.controller;

import com.microbio.application.dto.OrcamentoCreateDTO;
import com.microbio.application.dto.OrcamentoDTO;
import com.microbio.application.dto.OrcamentoUpdateDTO;
import com.microbio.application.model.OrcamentoStatus;
import com.microbio.application.service.OrcamentoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orcamentos")
@Tag(name = "Orçamentos", description = "Gerenciamento de orçamentos com perguntas dinâmicas")
public class OrcamentoController {

    private final OrcamentoService service;

    public OrcamentoController(OrcamentoService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Listar todos os orçamentos")
    public ResponseEntity<List<OrcamentoDTO>> getAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar orçamento por ID")
    public ResponseEntity<OrcamentoDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @GetMapping("/pessoa/{pessoaId}")
    @Operation(summary = "Listar orçamentos de uma pessoa")
    public ResponseEntity<List<OrcamentoDTO>> getByPessoa(@PathVariable Long pessoaId) {
        return ResponseEntity.ok(service.findByPessoa(pessoaId));
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Listar orçamentos por status")
    public ResponseEntity<List<OrcamentoDTO>> getByStatus(@PathVariable OrcamentoStatus status) {
        return ResponseEntity.ok(service.findByStatus(status));
    }

    @PostMapping
    @Operation(summary = "Criar orçamento com respostas às perguntas do serviço")
    public ResponseEntity<OrcamentoDTO> create(@RequestBody OrcamentoCreateDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar status/dados do orçamento")
    public ResponseEntity<OrcamentoDTO> update(@PathVariable Long id, @RequestBody OrcamentoUpdateDTO dto) {
        return ResponseEntity.ok(service.updateStatus(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remover orçamento")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
