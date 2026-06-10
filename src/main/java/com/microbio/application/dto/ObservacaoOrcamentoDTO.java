package com.microbio.application.dto;

import java.time.ZonedDateTime;

public record ObservacaoOrcamentoDTO(
    Long id,
    String texto,
    ZonedDateTime dataCriacao,
    String usuarioNome
) {}
