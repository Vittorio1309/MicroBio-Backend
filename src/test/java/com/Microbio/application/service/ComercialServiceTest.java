package com.microbio.application.service;

import com.microbio.application.dto.ComercialAnaliseDTO;
import com.microbio.application.model.Orcamento;
import com.microbio.application.model.OrcamentoStatus;
import com.microbio.application.repository.OrcamentoRepository;
import com.microbio.application.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ComercialServiceTest {

    @Mock
    private OrcamentoRepository orcamentoRepository;

    @Mock
    private ConfiguracaoService configuracaoService;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private ComercialService service;

    @Test
    void obterAnaliseComercial_calculatesMetricsCorrectly() {
        // Mocking configurations
        when(configuracaoService.getValor("prazo_acompanhamento_orcamentos", "48 horas")).thenReturn("24 horas");

        // Budgets setup
        LocalDateTime agora = LocalDateTime.now();
        
        // Converted 1 (Duration = 2 hours)
        Orcamento o1 = new Orcamento();
        o1.setStatus(OrcamentoStatus.FINALIZADO);
        o1.setDataCriacao(agora.minusHours(5));
        o1.setDataMovimentacao(agora.minusHours(3));
        
        // Converted 2 (Duration = 4 hours)
        Orcamento o2 = new Orcamento();
        o2.setStatus(OrcamentoStatus.ACEITO);
        o2.setDataCriacao(agora.minusHours(10));
        o2.setDataMovimentacao(agora.minusHours(6));
        
        // Lost
        Orcamento o3 = new Orcamento();
        o3.setStatus(OrcamentoStatus.REJEITADO);
        
        // Pending, delayed (unmoved since 30 hours ago, deadline is 24 hours)
        Orcamento o4 = new Orcamento();
        o4.setStatus(OrcamentoStatus.PENDENTE);
        o4.setDataCriacao(agora.minusHours(35));
        o4.setDataMovimentacao(agora.minusHours(30));

        // Pending, on time (unmoved since 2 hours ago)
        Orcamento o5 = new Orcamento();
        o5.setStatus(OrcamentoStatus.PENDENTE);
        o5.setDataCriacao(agora.minusHours(5));
        o5.setDataMovimentacao(agora.minusHours(2));

        when(orcamentoRepository.findAll()).thenReturn(List.of(o1, o2, o3, o4, o5));

        ComercialAnaliseDTO dto = service.obterAnaliseComercial("admin", "ADMIN", null);

        assertThat(dto.leadsRecebidos()).isEqualTo(5);
        assertThat(dto.leadsConvertidos()).isEqualTo(2);
        assertThat(dto.leadsPerdidos()).isEqualTo(1);
        assertThat(dto.taxaConversao()).isEqualTo(20.0); // 1 FINALIZADO / 5 total * 100
        // (2 hours + 4 hours) / 2 = 3.0 hours
        assertThat(dto.tempoMedioConversaoHoras()).isEqualTo(3.0);
        assertThat(dto.orcamentosAtrasados()).isEqualTo(1); // o4 is delayed, o5 is not
    }
}
