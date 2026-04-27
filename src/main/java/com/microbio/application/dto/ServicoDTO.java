package com.microbio.application.dto;

import java.math.BigDecimal;

public record ServicoDTO(
    Long id,
    String nome,
    String descricao,
    BigDecimal preco
) {
}