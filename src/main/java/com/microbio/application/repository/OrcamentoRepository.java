package com.microbio.application.repository;

import com.microbio.application.model.Orcamento;
import com.microbio.application.model.OrcamentoStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrcamentoRepository extends JpaRepository<Orcamento, Long> {
    List<Orcamento> findByPessoaId(Long pessoaId);
    List<Orcamento> findByServicoId(Long servicoId);
    List<Orcamento> findByStatus(OrcamentoStatus status);

    @Query("SELECT o FROM Orcamento o WHERE o.pessoa.id = ?1 AND (?2 IS NULL OR o.status = ?2) ORDER BY o.dataCriacao DESC")
    List<Orcamento> findByPessoaIdAndStatus(Long pessoaId, OrcamentoStatus status);
}
