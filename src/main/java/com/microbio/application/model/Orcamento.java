package com.microbio.application.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orcamento")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Orcamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime dataCriacao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrcamentoStatus status;

    private String observacao;

    private BigDecimal valorTotal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pessoa_id", nullable = false)
    private Pessoa pessoa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "servico_id", nullable = false)
    private Servico servico;

    @OneToMany(mappedBy = "orcamento", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<RespostaOrcamento> respostas = new ArrayList<>();

    @OneToMany(mappedBy = "orcamento", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ResultadoExame> resultados = new ArrayList<>();
}
