package com.microbio.application.dto;

import java.math.BigDecimal;

public record ServicoUpdateDTO(
    String nome,
    String descricao,
    BigDecimal preco
) {
}