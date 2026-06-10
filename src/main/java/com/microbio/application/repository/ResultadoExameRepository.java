package com.microbio.application.repository;

import com.microbio.application.model.ResultadoExame;
import com.microbio.application.model.ResultadoExameStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResultadoExameRepository extends JpaRepository<ResultadoExame, Long> {
    List<ResultadoExame> findByUsuarioId(Long usuarioId);
    long countByStatus(ResultadoExameStatus status);
    List<ResultadoExame> findTop10ByOrderByDataEmissaoDesc();
}
