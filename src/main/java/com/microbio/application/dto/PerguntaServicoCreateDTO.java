package com.microbio.application.dto;

public record PerguntaServicoCreateDTO(
    String pergunta,
    Boolean obrigatoria,
    Long servicoId
) {}
