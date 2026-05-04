package com.microbio.application.repository;

import com.microbio.application.model.RespostaOrcamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RespostaOrcamentoRepository extends JpaRepository<RespostaOrcamento, Long> {
    List<RespostaOrcamento> findByOrcamentoId(Long orcamentoId);
    void deleteByOrcamentoId(Long orcamentoId);
}
