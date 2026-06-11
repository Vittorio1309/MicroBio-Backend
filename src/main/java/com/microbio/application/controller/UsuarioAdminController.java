package com.microbio.application.controller;

import com.microbio.application.dto.UsuarioCreateDTO;
import com.microbio.application.dto.UsuarioResponseDTO;
import com.microbio.application.dto.UsuarioUpdateDTO;
import com.microbio.application.service.UsuarioAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/usuarios")
@Tag(name = "Admin - Usuários", description = "Gerenciamento de usuários do sistema")
public class UsuarioAdminController {

    private final UsuarioAdminService service;

    public UsuarioAdminController(UsuarioAdminService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Listar usuários, opcionalmente filtrados por role (ex: ?role=USER)")
    public ResponseEntity<List<UsuarioResponseDTO>> getAll(
            @RequestParam(required = false) String role) {
        if (role != null && !role.isBlank()) {
            return ResponseEntity.ok(service.findByRole(role));
        }
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/administradores")
    @Operation(summary = "Listar todos os usuários com perfil administrativo (ADMIN)")
    public ResponseEntity<List<UsuarioResponseDTO>> getAdministradores() {
        return ResponseEntity.ok(service.findAdministradores());
    }

    @PostMapping
    @Operation(summary = "Criar novo usuário com senha manual e role ROLE_USER")
    public ResponseEntity<UsuarioResponseDTO> create(@RequestBody UsuarioCreateDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar username e/ou senha de um usuário")
    public ResponseEntity<UsuarioResponseDTO> update(@PathVariable Long id, @RequestBody UsuarioUpdateDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remover usuário")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
