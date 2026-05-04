package com.microbio.application.repository;

import com.microbio.application.model.PerguntaServico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PerguntaServicoRepository extends JpaRepository<PerguntaServico, Long> {
    List<PerguntaServico> findByServicoId(Long servicoId);
    void deleteByServicoId(Long servicoId);
}
