package com.microbio.application.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "configuracao")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Configuracao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String chave;

    @Column(nullable = false)
    private String valor;

    public Configuracao(String chave, String valor) {
        this.chave = chave;
        this.valor = valor;
    }
}
