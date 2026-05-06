package com.microbio.application.service;

import com.microbio.application.dto.EstatisticasDTO;
import com.microbio.application.dto.ResultadoDashboardDTO;
import com.microbio.application.model.OrcamentoStatus;
import com.microbio.application.repository.OrcamentoRepository;
import com.microbio.application.repository.ResultadoExameRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    private final OrcamentoRepository orcamentoRepository;

    public DashboardService(OrcamentoRepository orcamentoRepository) {
        this.orcamentoRepository = orcamentoRepository;
    }

    /**
     * Retorna estatísticas totais de orçamentos
     */
    public EstatisticasDTO getEstatisticas() {
        long totalOrcamentos = orcamentoRepository.count();
        long totalPendente = orcamentoRepository.findByStatus(OrcamentoStatus.PENDENTE).size();
        long totalFinalizado = orcamentoRepository.findByStatus(OrcamentoStatus.FINALIZADO).size();

        return new EstatisticasDTO(totalOrcamentos, totalPendente, totalFinalizado);
    }

    /**
     * Retorna os últimos 10 resultados de exames com informações dos orçamentos
     */
    public List<ResultadoDashboardDTO> getResultadosRecentes() {
        return orcamentoRepository.findAll().stream()
                .sorted((o1, o2) -> o2.getDataCriacao().compareTo(o1.getDataCriacao()))
                .limit(10)
                .map(o -> new ResultadoDashboardDTO(
                        o.getId(),
                        o.getServico() != null ? o.getServico().getNome() : "N/A",
                        o.getPessoa() != null ? o.getPessoa().getNome() : "N/A",
                        o.getDataCriacao(),
                        o.getStatus(),
                        !o.getResultados().isEmpty()
                ))
                .toList();
    }
}


