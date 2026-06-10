package com.microbio.application.repository;

import com.microbio.application.model.HistoricoResponsavel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistoricoResponsavelRepository extends JpaRepository<HistoricoResponsavel, Long> {
    List<HistoricoResponsavel> findByOrcamentoIdOrderByDataAlteracaoDesc(Long orcamentoId);
}
