package com.microbio.application.service;

import com.microbio.application.dto.AdminDashboardStatsDTO;
import com.microbio.application.dto.EstatisticasDTO;
import com.microbio.application.dto.ResultadoDashboardDTO;
import com.microbio.application.model.ResultadoExameStatus;
import com.microbio.application.repository.ResultadoExameRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    private final ResultadoExameRepository resultadoExameRepository;

    public DashboardService(ResultadoExameRepository resultadoExameRepository) {
        this.resultadoExameRepository = resultadoExameRepository;
    }

    public EstatisticasDTO getEstatisticas() {
        long totalAnalises = resultadoExameRepository.count();
        long totalEmAndamento = resultadoExameRepository.countByStatus(ResultadoExameStatus.PENDENTE);
        long totalFinalizadas = resultadoExameRepository.countByStatus(ResultadoExameStatus.VISUALIZADO);
        return new EstatisticasDTO(totalAnalises, totalEmAndamento, totalFinalizadas);
    }

    public List<ResultadoDashboardDTO> getResultadosRecentes() {
        return resultadoExameRepository.findTop10ByOrderByDataEmissaoDesc()
                .stream()
                .map(r -> new ResultadoDashboardDTO(
                        r.getId(),
                        r.getUsuario().getUsername(),
                        r.getDescricao(),
                        r.getDataEmissao(),
                        r.getStatus()
                ))
                .toList();
    }

    public AdminDashboardStatsDTO getAdminStats() {
        EstatisticasDTO stats = getEstatisticas();
        List<ResultadoDashboardDTO> recentes = getResultadosRecentes();
        return new AdminDashboardStatsDTO(
                stats.totalAnalises(),
                stats.totalEmAndamento(),
                stats.totalFinalizadas(),
                recentes
        );
    }
}
