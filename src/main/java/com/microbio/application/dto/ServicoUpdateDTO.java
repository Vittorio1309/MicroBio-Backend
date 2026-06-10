package com.microbio.application.dto;

import java.math.BigDecimal;
import java.util.List;

public record ServicoUpdateDTO(
    String nome,
    String descricao,
    BigDecimal preco,
    String tipo,
    List<String> perguntas
) {
}