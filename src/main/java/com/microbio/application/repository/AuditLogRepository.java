package com.microbio.application.repository;

import com.microbio.application.model.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    @Query("SELECT a FROM AuditLog a WHERE " +
           "(?1 IS NULL OR a.dataHora >= ?1) AND " +
           "(?2 IS NULL OR a.dataHora <= ?2) AND " +
           "(?3 IS NULL OR LOWER(a.usuario) LIKE LOWER(CONCAT('%', ?3, '%'))) AND " +
           "(?4 IS NULL OR LOWER(a.acao) LIKE LOWER(CONCAT('%', ?4, '%')))")
    Page<AuditLog> filterLogs(LocalDateTime dataInicio, LocalDateTime dataFim, String usuario, String acao, Pageable pageable);
}
