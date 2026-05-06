package com.microbio.application.repository;

import com.microbio.application.model.ResultadoExame;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResultadoExameRepository extends JpaRepository<ResultadoExame, Long> {
    List<ResultadoExame> findByOrcamentoId(Long orcamentoId);
    List<ResultadoExame> findByOrcamentoPessoaId(Long pessoaId);
    boolean existsByOrcamentoId(Long orcamentoId);
}