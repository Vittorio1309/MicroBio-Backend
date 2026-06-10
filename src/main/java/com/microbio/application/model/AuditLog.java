package com.microbio.application.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_log")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String usuario;

    @Column(nullable = false)
    private String perfil;

    @Column(nullable = false)
    private LocalDateTime dataHora;

    @Column(nullable = false)
    private String acao;

    @Column(nullable = false)
    private String entidadeAfetada;

    private String identificadorRegistro;

    @Column(columnDefinition = "TEXT")
    private String detalhes;
}
