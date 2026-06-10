package com.microbio.application.service;

import com.microbio.application.dto.AuditLogDTO;
import com.microbio.application.model.AuditLog;
import com.microbio.application.repository.AuditLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
@Transactional
public class AuditLogService {

    private final AuditLogRepository repository;

    public AuditLogService(AuditLogRepository repository) {
        this.repository = repository;
    }

    public void log(String acao, String entidade, String idRegistro, String detalhes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = "sistema";
        String role = "SISTEMA";

        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal().toString())) {
            username = auth.getName();
            role = auth.getAuthorities().stream()
                    .map(g -> g.getAuthority())
                    .findFirst()
                    .orElse("USER");
            if (role.startsWith("ROLE_")) {
                role = role.substring(5);
            }
        }

        log(username, role, acao, entidade, idRegistro, detalhes);
    }

    public void log(String username, String role, String acao, String entidade, String idRegistro, String detalhes) {
        AuditLog auditLog = new AuditLog();
        auditLog.setUsuario(username);
        auditLog.setPerfil(role);
        auditLog.setDataHora(LocalDateTime.now());
        auditLog.setAcao(acao);
        auditLog.setEntidadeAfetada(entidade);
        auditLog.setIdentificadorRegistro(idRegistro);
        auditLog.setDetalhes(detalhes);

        repository.save(auditLog);
    }

    @Transactional(readOnly = true)
    public Page<AuditLogDTO> getLogs(LocalDate dataInicio, LocalDate dataFim, String usuario, String acao, Pageable pageable) {
        LocalDateTime inicio = dataInicio != null ? dataInicio.atStartOfDay() : null;
        LocalDateTime fim = dataFim != null ? dataFim.atTime(LocalTime.MAX) : null;

        return repository.filterLogs(inicio, fim, usuario, acao, pageable)
                .map(this::toDTO);
    }

    private AuditLogDTO toDTO(AuditLog log) {
        return new AuditLogDTO(
                log.getId(),
                log.getUsuario(),
                log.getPerfil(),
                log.getDataHora(),
                log.getAcao(),
                log.getEntidadeAfetada(),
                log.getIdentificadorRegistro(),
                log.getDetalhes()
        );
    }
}
