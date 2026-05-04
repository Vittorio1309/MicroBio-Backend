package com.microbio.application.dto;

public record PerguntaServicoDTO(
    Long id,
    String pergunta,
    Boolean obrigatoria,
    Long servicoId
) {}
