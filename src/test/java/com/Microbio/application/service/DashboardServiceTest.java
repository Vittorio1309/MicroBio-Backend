package com.microbio.application.service;

import com.microbio.application.dto.EstatisticasDTO;
import com.microbio.application.dto.ResultadoDashboardDTO;
import com.microbio.application.model.Orcamento;
import com.microbio.application.model.OrcamentoStatus;
import com.microbio.application.model.Pessoa;
import com.microbio.application.model.Servico;
import com.microbio.application.repository.OrcamentoRepository;
import com.microbio.application.repository.ResultadoExameRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock OrcamentoRepository orcamentoRepository;
    @Mock ResultadoExameRepository resultadoExameRepository;

    @InjectMocks
    DashboardService service;

    @Test
    void getEstatisticas_returnsCorrectCounts() {
        when(orcamentoRepository.count()).thenReturn(15L);
        when(orcamentoRepository.countByStatus(OrcamentoStatus.PENDENTE)).thenReturn(5L);
        when(orcamentoRepository.countByStatus(OrcamentoStatus.FINALIZADO)).thenReturn(8L);

        EstatisticasDTO stats = service.getEstatisticas();

        assertThat(stats.totalOrcamentos()).isEqualTo(15L);
        assertThat(stats.totalPendente()).isEqualTo(5L);
        assertThat(stats.totalFinalizado()).isEqualTo(8L);
    }

    @Test
    void getEstatisticas_usesCountByStatusNotFindAll() {
        when(orcamentoRepository.count()).thenReturn(0L);
        when(orcamentoRepository.countByStatus(any())).thenReturn(0L);

        service.getEstatisticas();

        verify(orcamentoRepository, never()).findAll();
        verify(orcamentoRepository, never()).findByStatus(any());
    }

    @Test
    void getResultadosRecentes_returnsMappedList() {
        Orcamento o1 = orcamento(1L);
        Orcamento o2 = orcamento(2L);
        when(orcamentoRepository.findRecentesComDetalhes(any(Pageable.class))).thenReturn(List.of(o1, o2));
        when(resultadoExameRepository.existsByOrcamentoId(1L)).thenReturn(true);
        when(resultadoExameRepository.existsByOrcamentoId(2L)).thenReturn(false);

        List<ResultadoDashboardDTO> result = service.getResultadosRecentes();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).orcamentoId()).isEqualTo(1L);
        assertThat(result.get(0).temResultadoExame()).isTrue();
        assertThat(result.get(1).temResultadoExame()).isFalse();
    }

    @Test
    void getResultadosRecentes_requestsExactly10Records() {
        when(orcamentoRepository.findRecentesComDetalhes(any(Pageable.class))).thenReturn(List.of());

        service.getResultadosRecentes();

        verify(orcamentoRepository).findRecentesComDetalhes(argThat(p -> p.getPageSize() == 10));
    }

    @Test
    void getResultadosRecentes_doesNotLoadAllOrcamentos() {
        when(orcamentoRepository.findRecentesComDetalhes(any(Pageable.class))).thenReturn(List.of());

        service.getResultadosRecentes();

        verify(orcamentoRepository, never()).findAll();
    }

    private Orcamento orcamento(Long id) {
        Pessoa pessoa = new Pessoa("Cliente Teste", "cliente@ex.com", "11999");
        pessoa.setId(1L);

        Servico servico = new Servico();
        servico.setId(1L);
        servico.setNome("Análise de Água");
        servico.setPreco(BigDecimal.valueOf(350));

        Orcamento o = new Orcamento();
        o.setId(id);
        o.setDataCriacao(LocalDateTime.now());
        o.setStatus(OrcamentoStatus.PENDENTE);
        o.setPessoa(pessoa);
        o.setServico(servico);
        return o;
    }
}
