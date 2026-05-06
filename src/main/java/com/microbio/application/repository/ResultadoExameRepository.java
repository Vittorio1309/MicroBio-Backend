package com.microbio.application.repository;

import com.microbio.application.model.ResultadoExame;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResultadoExameRepository extends JpaRepository<ResultadoExame, Long> {
    List<ResultadoExame> findByOrcamentoId(Long orcamentoId);
    List<ResultadoExame> findByOrcamentoPessoaId(Long pessoaId);
    
    @Query("SELECT r.orcamento.id FROM ResultadoExame r WHERE r.orcamento.id = ?1 LIMIT 1")
    boolean existsByOrcamentoId(Long orcamentoId);
}
