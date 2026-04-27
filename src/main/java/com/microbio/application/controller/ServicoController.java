package com.microbio.application.controller;

import com.microbio.application.dto.ServicoCreateDTO;
import com.microbio.application.dto.ServicoDTO;
import com.microbio.application.dto.ServicoUpdateDTO;
import com.microbio.application.model.Servico;
import com.microbio.application.service.ServicoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/servicos")
public class ServicoController {

    private final ServicoService service;

    public ServicoController(ServicoService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<ServicoDTO>> getAll() {
        List<ServicoDTO> servicos = service.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(servicos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServicoDTO> getById(@PathVariable Long id) {
        Optional<Servico> servico = service.findById(id);
        if (servico.isPresent()) {
            return ResponseEntity.ok(toDTO(servico.get()));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<ServicoDTO> create(@RequestBody ServicoCreateDTO dto) {
        Servico servico = new Servico();
        servico.setNome(dto.nome());
        servico.setDescricao(dto.descricao());
        servico.setPreco(dto.preco());
        Servico saved = service.save(servico);
        return ResponseEntity.ok(toDTO(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ServicoDTO> update(@PathVariable Long id, @RequestBody ServicoUpdateDTO dto) {
        Optional<Servico> existing = service.findById(id);
        if (existing.isPresent()) {
            Servico servico = existing.get();
            if (dto.nome() != null) servico.setNome(dto.nome());
            if (dto.descricao() != null) servico.setDescricao(dto.descricao());
            if (dto.preco() != null) servico.setPreco(dto.preco());
            Servico updated = service.save(servico);
            return ResponseEntity.ok(toDTO(updated));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (service.findById(id).isPresent()) {
            service.deleteById(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    private ServicoDTO toDTO(Servico servico) {
        return new ServicoDTO(
                servico.getId(),
                servico.getNome(),
                servico.getDescricao(),
                servico.getPreco()
        );
    }
}