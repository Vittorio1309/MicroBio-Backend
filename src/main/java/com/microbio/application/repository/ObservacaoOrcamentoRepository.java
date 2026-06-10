package com.microbio.application.repository;

import com.microbio.application.model.ObservacaoOrcamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ObservacaoOrcamentoRepository extends JpaRepository<ObservacaoOrcamento, Long> {
    List<ObservacaoOrcamento> findByOrcamentoIdOrderByDataCriacaoAsc(Long orcamentoId);
}
