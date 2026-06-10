package com.microbio.application.controller;

import com.microbio.application.dto.PerguntaServicoCreateDTO;
import com.microbio.application.dto.PerguntaServicoDTO;
import com.microbio.application.service.PerguntaServicoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/perguntas")
@Tag(name = "Perguntas de Serviço", description = "Perguntas dinâmicas associadas a cada serviço")
public class PerguntaServicoController {

    private final PerguntaServicoService service;

    public PerguntaServicoController(PerguntaServicoService service) {
        this.service = service;
    }

    @GetMapping("/servico/{servicoId}")
    @Operation(summary = "Listar perguntas de um serviço")
    public ResponseEntity<List<PerguntaServicoDTO>> getByServico(@PathVariable Long servicoId) {
        return ResponseEntity.ok(service.findByServico(servicoId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar pergunta por ID")
    public ResponseEntity<PerguntaServicoDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ADMIN_MASTER')")
    @Operation(summary = "Criar pergunta para um serviço (Admin)")
    public ResponseEntity<PerguntaServicoDTO> create(@RequestBody PerguntaServicoCreateDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ADMIN_MASTER')")
    @Operation(summary = "Atualizar pergunta (Admin)")
    public ResponseEntity<PerguntaServicoDTO> update(@PathVariable Long id, @RequestBody PerguntaServicoCreateDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ADMIN_MASTER')")
    @Operation(summary = "Remover pergunta (Admin)")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
