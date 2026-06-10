package com.microbio.application.controller;

import com.microbio.application.dto.ServicoCreateDTO;
import com.microbio.application.dto.ServicoDTO;
import com.microbio.application.dto.ServicoUpdateDTO;
import com.microbio.application.service.ServicoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/servicos")
@Tag(name = "Serviços", description = "Gerenciamento de serviços laboratoriais")
public class ServicoController {

    private final ServicoService service;

    public ServicoController(ServicoService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Listar todos os serviços")
    public ResponseEntity<List<ServicoDTO>> getAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar serviço por ID")
    public ResponseEntity<ServicoDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ADMIN_MASTER')")
    @Operation(summary = "Cadastrar novo serviço (Admin)")
    public ResponseEntity<ServicoDTO> create(@RequestBody ServicoCreateDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ADMIN_MASTER')")
    @Operation(summary = "Atualizar serviço (Admin)")
    public ResponseEntity<ServicoDTO> update(@PathVariable Long id, @RequestBody ServicoUpdateDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ADMIN_MASTER')")
    @Operation(summary = "Remover serviço (Admin)")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
