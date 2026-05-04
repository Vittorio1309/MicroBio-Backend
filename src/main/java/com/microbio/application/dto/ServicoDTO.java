package com.microbio.application.dto;

import java.math.BigDecimal;
import java.util.List;

public record ServicoDTO(
    Long id,
    String nome,
    String descricao,
    BigDecimal preco,
    List<PerguntaServicoDTO> perguntas
) {}
