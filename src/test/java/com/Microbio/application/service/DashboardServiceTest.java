package com.microbio.application.service;

import com.microbio.application.dto.EstatisticasDTO;
import com.microbio.application.dto.ResultadoDashboardDTO;
import com.microbio.application.model.ResultadoExame;
import com.microbio.application.model.ResultadoExameStatus;
import com.microbio.application.model.Usuario;
import com.microbio.application.repository.ResultadoExameRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock ResultadoExameRepository resultadoExameRepository;

    @InjectMocks
    DashboardService service;

    @Test
    void getEstatisticas_returnsCorrectCounts() {
        when(resultadoExameRepository.count()).thenReturn(15L);
        when(resultadoExameRepository.countByStatus(ResultadoExameStatus.PENDENTE)).thenReturn(5L);
        when(resultadoExameRepository.countByStatus(ResultadoExameStatus.VISUALIZADO)).thenReturn(10L);

        EstatisticasDTO stats = service.getEstatisticas();

        assertThat(stats.totalAnalises()).isEqualTo(15L);
        assertThat(stats.totalEmAndamento()).isEqualTo(5L);
        assertThat(stats.totalFinalizadas()).isEqualTo(10L);
    }

    @Test
    void getEstatisticas_usesCountByStatusNotFindAll() {
        when(resultadoExameRepository.count()).thenReturn(0L);
        when(resultadoExameRepository.countByStatus(any())).thenReturn(0L);

        service.getEstatisticas();

        verify(resultadoExameRepository, never()).findAll();
    }

    @Test
    void getResultadosRecentes_returnsMappedList() {
        ResultadoExame r1 = resultado(1L, "Análise de solo", ResultadoExameStatus.EM_ANDAMENTO);
        ResultadoExame r2 = resultado(2L, "Análise foliar", ResultadoExameStatus.FINALIZADO);
        when(resultadoExameRepository.findTop10ByOrderByDataEmissaoDesc()).thenReturn(List.of(r1, r2));

        List<ResultadoDashboardDTO> result = service.getResultadosRecentes();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).id()).isEqualTo(1L);
        assertThat(result.get(0).descricao()).isEqualTo("Análise de solo");
        assertThat(result.get(0).status()).isEqualTo(ResultadoExameStatus.EM_ANDAMENTO);
        assertThat(result.get(1).status()).isEqualTo(ResultadoExameStatus.FINALIZADO);
    }

    @Test
    void getResultadosRecentes_usesTop10Query() {
        when(resultadoExameRepository.findTop10ByOrderByDataEmissaoDesc()).thenReturn(List.of());

        service.getResultadosRecentes();

        verify(resultadoExameRepository).findTop10ByOrderByDataEmissaoDesc();
        verify(resultadoExameRepository, never()).findAll();
    }

    private ResultadoExame resultado(Long id, String descricao, ResultadoExameStatus status) {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setUsername("cliente_teste");
        usuario.setSenha("hash");
        usuario.setRole("USER");

        ResultadoExame r = new ResultadoExame();
        r.setId(id);
        r.setDescricao(descricao);
        r.setDataEmissao(LocalDateTime.now());
        r.setStatus(status);
        r.setUsuario(usuario);
        return r;
    }
}
