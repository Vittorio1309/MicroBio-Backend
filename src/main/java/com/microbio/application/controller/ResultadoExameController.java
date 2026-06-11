package com.microbio.application.controller;

import com.microbio.application.dto.ResultadoExameCreateDTO;
import com.microbio.application.dto.ResultadoExameDTO;
import com.microbio.application.dto.ResultadoExameUpdateDTO;
import com.microbio.application.service.ResultadoExameService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

    @GetMapping("/meus")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Listar análises do usuário autenticado")
    public ResponseEntity<List<ResultadoExameDTO>> getMeus(Authentication authentication) {
        return ResponseEntity.ok(service.findByUsuarioAutenticado(authentication.getName()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar resultado por ID")
    public ResponseEntity<ResultadoExameDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cadastrar resultado de exame (Admin)")
    public ResponseEntity<ResultadoExameDTO> create(@RequestBody ResultadoExameCreateDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Atualizar resultado de exame (Admin)")
    public ResponseEntity<ResultadoExameDTO> update(@PathVariable Long id, @RequestBody ResultadoExameUpdateDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Remover resultado de exame (Admin)")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/visualizar")
    @Operation(summary = "Marcar resultado como visualizado (usuário dono)")
    public ResponseEntity<ResultadoExameDTO> visualizar(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(service.visualizar(id, authentication.getName()));
    }

    @PostMapping(value = "/{id}/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Fazer upload do PDF do resultado (Admin)")
    public ResponseEntity<ResultadoExameDTO> upload(@PathVariable Long id,
                                                    @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(service.uploadArquivo(id, file));
    }

    @GetMapping("/{id}/download")
    @Operation(summary = "Baixar o PDF do resultado (Admin ou dono do resultado)")
    public ResponseEntity<Resource> download(@PathVariable Long id, Authentication authentication) {
        Resource resource = service.downloadArquivo(id, authentication.getName());
        String filename = resource.getFilename() != null ? resource.getFilename() : "resultado.pdf";
        // Remove prefix "id-uuid-" para nome amigável
        String friendlyName = filename.replaceFirst("^\\d+-[a-f0-9-]+-", "");
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + friendlyName + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }
}
