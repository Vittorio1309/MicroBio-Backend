package com.microbio.application.dto;

import java.math.BigDecimal;
import java.util.List;

public record ServicoCreateDTO(
    String nome,
    String descricao,
    BigDecimal preco,
    String tipo,
    List<String> perguntas
) {
}