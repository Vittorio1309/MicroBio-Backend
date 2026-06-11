package com.microbio.application.controller;

import com.microbio.application.dto.UsuarioResponseDTO;
import com.microbio.application.service.UsuarioAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@Tag(name = "Usuários", description = "Consulta de usuários do sistema")
public class UsuarioController {

    private final UsuarioAdminService service;

    public UsuarioController(UsuarioAdminService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar usuários, opcionalmente filtrados por role (ex: ?role=USER)")
    public ResponseEntity<List<UsuarioResponseDTO>> getAll(
            @RequestParam(required = false) String role) {
        if (role != null && !role.isBlank()) {
            return ResponseEntity.ok(service.findByRole(role));
        }
        return ResponseEntity.ok(service.findAll());
    }
}
