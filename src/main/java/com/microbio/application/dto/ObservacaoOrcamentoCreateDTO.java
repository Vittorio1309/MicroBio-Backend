package com.microbio.application.dto;

import jakarta.validation.constraints.NotBlank;

public record ObservacaoOrcamentoCreateDTO(
    @NotBlank(message = "O texto da observação não pode ser vazio.")
    String texto
) {}
