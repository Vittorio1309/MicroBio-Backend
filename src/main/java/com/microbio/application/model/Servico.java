package com.microbio.application.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "servico")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Servico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String nome;

    private String descricao;

    private BigDecimal preco;

    @Column
    private String tipo;

    @OneToMany(mappedBy = "servico", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Orcamento> orcamentos = new ArrayList<>();

    @OneToMany(mappedBy = "servico", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<PerguntaServico> perguntas = new ArrayList<>();
}
