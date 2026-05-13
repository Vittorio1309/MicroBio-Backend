package com.microbio.application.service;

import com.microbio.application.dto.EstatisticasDTO;
import com.microbio.application.dto.ResultadoDashboardDTO;
import com.microbio.application.model.OrcamentoStatus;
import com.microbio.application.repository.OrcamentoRepository;
import com.microbio.application.repository.ResultadoExameRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    private final OrcamentoRepository orcamentoRepository;
    private final ResultadoExameRepository resultadoExameRepository;

    public DashboardService(OrcamentoRepository orcamentoRepository,
                            ResultadoExameRepository resultadoExameRepository) {
        this.orcamentoRepository = orcamentoRepository;
        this.resultadoExameRepository = resultadoExameRepository;
    }

    public EstatisticasDTO getEstatisticas() {
        long totalOrcamentos = orcamentoRepository.count();
        long totalPendente = orcamentoRepository.countByStatus(OrcamentoStatus.PENDENTE);
        long totalFinalizado = orcamentoRepository.countByStatus(OrcamentoStatus.FINALIZADO);
        return new EstatisticasDTO(totalOrcamentos, totalPendente, totalFinalizado);
    }

    public List<ResultadoDashboardDTO> getResultadosRecentes() {
        return orcamentoRepository.findRecentesComDetalhes(PageRequest.of(0, 10))
                .stream()
                .map(o -> new ResultadoDashboardDTO(
                        o.getId(),
                        o.getServico() != null ? o.getServico().getNome() : "N/A",
                        o.getPessoa() != null ? o.getPessoa().getNome() : "N/A",
                        o.getDataCriacao(),
                        o.getStatus(),
                        resultadoExameRepository.existsByOrcamentoId(o.getId())
                ))
                .toList();
    }
}


