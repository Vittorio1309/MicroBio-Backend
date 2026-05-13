package com.microbio.application.controller;

import com.microbio.application.dto.OrcamentoCreateDTO;
import com.microbio.application.dto.OrcamentoDTO;
import com.microbio.application.dto.OrcamentoUpdateDTO;
import com.microbio.application.dto.MeusPedidosDTO;
import com.microbio.application.exception.BusinessException;
import com.microbio.application.model.OrcamentoStatus;
import com.microbio.application.repository.UsuarioRepository;
import com.microbio.application.service.OrcamentoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orcamentos")
@Tag(name = "Orçamentos", description = "Gerenciamento de orçamentos com perguntas dinâmicas")
public class OrcamentoController {

    private final OrcamentoService service;
    private final UsuarioRepository usuarioRepository;

    public OrcamentoController(OrcamentoService service, UsuarioRepository usuarioRepository) {
        this.service = service;
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping
    @Operation(summary = "Listar todos os orçamentos (paginado)")
    public ResponseEntity<Page<OrcamentoDTO>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(service.findAll(PageRequest.of(page, size, Sort.by("dataCriacao").descending())));
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

    @GetMapping("/meus-pedidos")
    @Operation(summary = "Retorna orçamentos do usuário autenticado com filtro opcional por status")
    public ResponseEntity<List<MeusPedidosDTO>> meusPedidos(
            Authentication authentication,
            @RequestParam(required = false) OrcamentoStatus status) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String username = authentication.getName();
        Long pessoaId = getPessoaIdFromUsername(username);

        if (pessoaId == null) {
            throw new BusinessException("Usuário não possui pessoa vinculada. Use PUT /api/auth/vincular-pessoa/{pessoaId}");
        }

        return ResponseEntity.ok(service.getMeusPedidos(pessoaId, status));
    }

    private Long getPessoaIdFromUsername(String username) {
        return usuarioRepository.findByUsername(username)
                .map(u -> u.getPessoaId())
                .orElse(null);
    }
}
