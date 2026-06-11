package com.microbio.application.controller;

import com.microbio.application.service.ConfiguracaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/configuracoes")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin - Configurações", description = "Endpoints de configurações administrativas")
public class ConfiguracaoController {

    private final ConfiguracaoService service;

    public ConfiguracaoController(ConfiguracaoService service) {
        this.service = service;
    }

    @GetMapping("/{chave}")
    @Operation(summary = "Obter valor de uma configuração")
    public ResponseEntity<Map<String, String>> getValor(
            @PathVariable String chave,
            @RequestParam(defaultValue = "") String valorPadrao) {
        String valor = service.getValor(chave, valorPadrao);
        return ResponseEntity.ok(Map.of("chave", chave, "valor", valor));
    }

    @PostMapping("/{chave}")
    @Operation(summary = "Salvar ou atualizar uma configuração")
    public ResponseEntity<Map<String, String>> salvar(
            @PathVariable String chave,
            @RequestBody Map<String, String> body) {
        String valor = body.get("valor");
        if (valor == null) {
            return ResponseEntity.badRequest().build();
        }
        service.salvar(chave, valor);
        return ResponseEntity.ok(Map.of("chave", chave, "valor", valor));
    }
}
