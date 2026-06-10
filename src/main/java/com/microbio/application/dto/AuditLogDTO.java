package com.microbio.application.dto;

import java.time.LocalDateTime;

public record AuditLogDTO(
    Long id,
    String usuario,
    String perfil,
    LocalDateTime dataHora,
    String acao,
    String entidadeAfetada,
    String identificadorRegistro,
    String detalhes
) {}
