package com.microbio.application.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "historico_responsavel_orcamento")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HistoricoResponsavel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orcamento_id", nullable = false)
    private Orcamento orcamento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsavel_anterior_id")
    private Usuario responsavelAnterior;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsavel_novo_id")
    private Usuario responsavelNovo;

    @Column(nullable = false)
    private LocalDateTime dataAlteracao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "alterado_por_id", nullable = false)
    private Usuario alteradoPor;
}
