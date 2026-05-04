package com.microbio.application.dto;

public record RespostaDTO(
    Long perguntaId,
    String pergunta,
    String resposta
) {}
