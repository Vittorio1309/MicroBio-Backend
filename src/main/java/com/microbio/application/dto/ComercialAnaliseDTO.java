package com.microbio.application.dto;

import java.math.BigDecimal;

public record ComercialAnaliseDTO(
    long leadsRecebidos,
    long leadsConvertidos,
    long leadsPerdidos,
    double taxaConversao,
    double tempoMedioConversaoHoras,
    long orcamentosAtrasados,
    long leadsPendentes,
    long leadsAceitos,
    long leadsRejeitados,
    long leadsConcluidos,
    long leadsEmAberto,
    BigDecimal valorPotencial,
    BigDecimal valorEmRisco,
    BigDecimal valorConvertido,
    BigDecimal valorPerdido
) {}
