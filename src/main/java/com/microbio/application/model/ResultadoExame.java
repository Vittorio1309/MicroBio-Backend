package com.microbio.application.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "resultado_exame")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResultadoExame {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String descricao;

    @Column(nullable = false)
    private LocalDateTime dataEmissao;

    @Column(columnDefinition = "TEXT")
    private String laudo;

    private String arquivoUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ResultadoExameStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
}
