package com.microbio.application.controller;

import com.microbio.application.dto.OrcamentoCreateDTO;
import com.microbio.application.dto.OrcamentoDTO;
import com.microbio.application.dto.OrcamentoUpdateDTO;
import com.microbio.application.model.Orcamento;
import com.microbio.application.model.Pessoa;
import com.microbio.application.model.Servico;
import com.microbio.application.service.OrcamentoService;
import com.microbio.application.service.PessoaService;
import com.microbio.application.service.ServicoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orcamentos")
public class OrcamentoController {

    private final OrcamentoService orcamentoService;
    private final PessoaService pessoaService;
    private final ServicoService servicoService;

    public OrcamentoController(OrcamentoService orcamentoService,
                               PessoaService pessoaService,
                               ServicoService servicoService) {
        this.orcamentoService = orcamentoService;
        this.pessoaService = pessoaService;
        this.servicoService = servicoService;
    }

    @GetMapping
    public ResponseEntity<List<OrcamentoDTO>> getAll() {
        List<OrcamentoDTO> orcamentos = orcamentoService.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(orcamentos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrcamentoDTO> getById(@PathVariable Long id) {
        Optional<Orcamento> orcamento = orcamentoService.findById(id);
        if (orcamento.isPresent()) {
            return ResponseEntity.ok(toDTO(orcamento.get()));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<OrcamentoDTO> create(@RequestBody OrcamentoCreateDTO dto) {
        Optional<Pessoa> pessoa = pessoaService.findById(dto.pessoaId());
        Optional<Servico> servico = servicoService.findById(dto.servicoId());
        if (pessoa.isEmpty() || servico.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        Orcamento orcamento = new Orcamento();
        orcamento.setDataCriacao(LocalDateTime.now());
        orcamento.setStatus(dto.status());
        orcamento.setObservacao(dto.observacao());
        orcamento.setPessoa(pessoa.get());
        orcamento.setServico(servico.get());

        Orcamento saved = orcamentoService.save(orcamento);
        return ResponseEntity.ok(toDTO(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrcamentoDTO> update(@PathVariable Long id, @RequestBody OrcamentoUpdateDTO dto) {
        Optional<Orcamento> existing = orcamentoService.findById(id);
        if (existing.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Optional<Pessoa> pessoa = dto.pessoaId() != null ? pessoaService.findById(dto.pessoaId()) : Optional.of(existing.get().getPessoa());
        Optional<Servico> servico = dto.servicoId() != null ? servicoService.findById(dto.servicoId()) : Optional.of(existing.get().getServico());
        if (pessoa.isEmpty() || servico.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        Orcamento orcamento = existing.get();
        if (dto.status() != null) orcamento.setStatus(dto.status());
        if (dto.observacao() != null) orcamento.setObservacao(dto.observacao());
        orcamento.setPessoa(pessoa.get());
        orcamento.setServico(servico.get());

        Orcamento updated = orcamentoService.save(orcamento);
        return ResponseEntity.ok(toDTO(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (orcamentoService.findById(id).isPresent()) {
            orcamentoService.deleteById(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    private OrcamentoDTO toDTO(Orcamento orcamento) {
        return new OrcamentoDTO(
                orcamento.getId(),
                orcamento.getDataCriacao(),
                orcamento.getStatus(),
                orcamento.getObservacao(),
                orcamento.getPessoa() != null ? orcamento.getPessoa().getId() : null,
                orcamento.getServico() != null ? orcamento.getServico().getId() : null
        );
    }
}