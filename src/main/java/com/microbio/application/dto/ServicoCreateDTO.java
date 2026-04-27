package com.microbio.application.dto;

import java.math.BigDecimal;

public record ServicoCreateDTO(
    String nome,
    String descricao,
    BigDecimal preco
) {
}