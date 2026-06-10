package com.microbio.application.service;

import com.microbio.application.dto.MeusPedidosDTO;
import com.microbio.application.dto.OrcamentoCreateDTO;
import com.microbio.application.dto.OrcamentoDTO;
import com.microbio.application.dto.RespostaDTO;
import com.microbio.application.exception.BusinessException;
import com.microbio.application.exception.ResourceNotFoundException;
import com.microbio.application.model.*;
import com.microbio.application.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrcamentoServiceTest {

    @Mock OrcamentoRepository orcamentoRepository;
    @Mock PessoaRepository pessoaRepository;
    @Mock ServicoRepository servicoRepository;
    @Mock PerguntaServicoRepository perguntaRepository;
    @Mock RespostaOrcamentoRepository respostaRepository;
    @Mock ObservacaoOrcamentoRepository observacaoRepository;
    @Mock UsuarioRepository usuarioRepository;
    @Mock AuditLogService auditLogService;
    @Mock HistoricoResponsavelRepository historicoResponsavelRepository;

    @InjectMocks
    OrcamentoService service;

    @Test
    void findAll_returnsPaginatedDTOs() {
        Orcamento o = orcamento(1L, OrcamentoStatus.PENDENTE);
        Page<Orcamento> page = new PageImpl<>(List.of(o));
        when(orcamentoRepository.findAll(any(PageRequest.class))).thenReturn(page);
        when(respostaRepository.findByOrcamentoId(1L)).thenReturn(List.of());

        Page<OrcamentoDTO> result = service.findAll(PageRequest.of(0, 20));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).id()).isEqualTo(1L);
    }

    @Test
    void findById_whenExists_returnsDTO() {
        Orcamento o = orcamento(1L, OrcamentoStatus.PENDENTE);
        when(orcamentoRepository.findById(1L)).thenReturn(Optional.of(o));
        when(respostaRepository.findByOrcamentoId(1L)).thenReturn(List.of());

        OrcamentoDTO result = service.findById(1L);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.status()).isEqualTo(OrcamentoStatus.PENDENTE);
    }

    @Test
    void findById_whenNotFound_throwsResourceNotFoundException() {
        when(orcamentoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void create_withNoMandatoryQuestions_createsSuccessfully() {
        Pessoa pessoa = pessoa(1L);
        Servico servico = servico(1L, BigDecimal.valueOf(350));

        Orcamento saved = new Orcamento();
        saved.setId(1L);
        saved.setDataCriacao(LocalDateTime.now());
        saved.setStatus(OrcamentoStatus.PENDENTE);
        saved.setPessoa(pessoa);
        saved.setServico(servico);
        saved.setValorTotal(BigDecimal.valueOf(350));

        when(pessoaRepository.findById(1L)).thenReturn(Optional.of(pessoa));
        when(servicoRepository.findById(1L)).thenReturn(Optional.of(servico));
        when(perguntaRepository.findByServicoId(1L)).thenReturn(List.of());
        when(orcamentoRepository.save(any())).thenReturn(saved);
        when(orcamentoRepository.findById(1L)).thenReturn(Optional.of(saved));
        when(respostaRepository.findByOrcamentoId(1L)).thenReturn(List.of());

        OrcamentoCreateDTO dto = new OrcamentoCreateDTO(1L, 1L, null, null, List.of());
        OrcamentoDTO result = service.create(dto);

        assertThat(result.status()).isEqualTo(OrcamentoStatus.PENDENTE);
        assertThat(result.valorTotal()).isEqualByComparingTo(BigDecimal.valueOf(350));
    }

    @Test
    void create_whenMandatoryQuestionMissing_throwsBusinessException() {
        Pessoa pessoa = pessoa(1L);
        Servico servico = servico(1L, BigDecimal.valueOf(350));

        PerguntaServico pergunta = new PerguntaServico();
        pergunta.setId(10L);
        pergunta.setPergunta("Tipo de amostra?");
        pergunta.setObrigatoria(true);

        when(pessoaRepository.findById(1L)).thenReturn(Optional.of(pessoa));
        when(servicoRepository.findById(1L)).thenReturn(Optional.of(servico));
        when(perguntaRepository.findByServicoId(1L)).thenReturn(List.of(pergunta));

        OrcamentoCreateDTO dto = new OrcamentoCreateDTO(1L, 1L, null, null, List.of());

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Tipo de amostra?");
    }

    @Test
    void create_whenMandatoryQuestionAnswered_createsSuccessfully() {
        Pessoa pessoa = pessoa(1L);
        Servico servico = servico(1L, BigDecimal.valueOf(280));

        PerguntaServico pergunta = new PerguntaServico();
        pergunta.setId(10L);
        pergunta.setPergunta("Tipo de amostra?");
        pergunta.setObrigatoria(true);

        Orcamento saved = orcamento(1L, OrcamentoStatus.PENDENTE);

        when(pessoaRepository.findById(1L)).thenReturn(Optional.of(pessoa));
        when(servicoRepository.findById(1L)).thenReturn(Optional.of(servico));
        when(perguntaRepository.findByServicoId(1L)).thenReturn(List.of(pergunta));
        when(perguntaRepository.findById(10L)).thenReturn(Optional.of(pergunta));
        when(orcamentoRepository.save(any())).thenReturn(saved);
        when(orcamentoRepository.findById(1L)).thenReturn(Optional.of(saved));
        when(respostaRepository.findByOrcamentoId(1L)).thenReturn(List.of());

        RespostaDTO resposta = new RespostaDTO(10L, "Tipo de amostra?", "Água potável");
        OrcamentoCreateDTO dto = new OrcamentoCreateDTO(1L, 1L, null, null, List.of(resposta));

        OrcamentoDTO result = service.create(dto);

        assertThat(result.id()).isEqualTo(1L);
        verify(respostaRepository).save(any());
    }

    @Test
    void getMeusPedidos_returnsMappedList_withoutExtraQueries() {
        Servico servico = servico(1L, BigDecimal.valueOf(350));
        servico.setNome("Análise de Água");

        Orcamento o = new Orcamento();
        o.setId(1L);
        o.setDataCriacao(LocalDateTime.now());
        o.setStatus(OrcamentoStatus.PENDENTE);
        o.setPessoa(pessoa(1L));
        o.setServico(servico);
        o.setValorTotal(BigDecimal.valueOf(350));
        o.setObservacao("Urgente");

        when(orcamentoRepository.findByPessoaIdAndStatus(1L, null)).thenReturn(List.of(o));

        List<MeusPedidosDTO> result = service.getMeusPedidos(1L, null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).nomeServico()).isEqualTo("Análise de Água");
        assertThat(result.get(0).status()).isEqualTo(OrcamentoStatus.PENDENTE);
        verifyNoInteractions(respostaRepository);
    }

    @Test
    void getMeusPedidos_filterByStatus_passesStatusToRepository() {
        when(orcamentoRepository.findByPessoaIdAndStatus(1L, OrcamentoStatus.ACEITO)).thenReturn(List.of());

        List<MeusPedidosDTO> result = service.getMeusPedidos(1L, OrcamentoStatus.ACEITO);

        assertThat(result).isEmpty();
        verify(orcamentoRepository).findByPessoaIdAndStatus(1L, OrcamentoStatus.ACEITO);
    }

    @Test
    void transferirResponsavel_whenExecutorIsMaster_updatesResponsavel() {
        Orcamento o = orcamento(1L, OrcamentoStatus.PENDENTE);
        Usuario executor = new Usuario("master", "encoded", "ADMIN_MASTER");
        Usuario novo = new Usuario("new_admin", "encoded", "ADMIN");

        when(orcamentoRepository.findById(1L)).thenReturn(Optional.of(o));
        when(usuarioRepository.findByUsername("master")).thenReturn(Optional.of(executor));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(novo));
        when(orcamentoRepository.save(any(Orcamento.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrcamentoDTO result = service.transferirResponsavel(1L, 2L, "master");

        assertThat(result.responsavelNome()).isEqualTo("new_admin");
        verify(historicoResponsavelRepository).save(any(HistoricoResponsavel.class));
        verify(auditLogService).log(eq("master"), eq("ADMIN_MASTER"), eq("TRANSFERENCIA_RESPONSAVEL"), eq("Orcamento"), eq("1"), anyString());
    }

    @Test
    void transferirResponsavel_whenExecutorIsNotMaster_throwsBusinessException() {
        Orcamento o = orcamento(1L, OrcamentoStatus.PENDENTE);
        Usuario executor = new Usuario("regular_admin", "encoded", "ADMIN");

        when(orcamentoRepository.findById(1L)).thenReturn(Optional.of(o));
        when(usuarioRepository.findByUsername("regular_admin")).thenReturn(Optional.of(executor));

        assertThatThrownBy(() -> service.transferirResponsavel(1L, 2L, "regular_admin"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Apenas administradores Master podem transferir responsáveis.");
    }

    private Orcamento orcamento(Long id, OrcamentoStatus status) {
        Orcamento o = new Orcamento();
        o.setId(id);
        o.setDataCriacao(LocalDateTime.now());
        o.setStatus(status);
        o.setPessoa(pessoa(1L));
        o.setServico(servico(1L, BigDecimal.valueOf(100)));
        o.setValorTotal(BigDecimal.valueOf(100));
        return o;
    }

    private Pessoa pessoa(Long id) {
        Pessoa p = new Pessoa("Cliente Teste", "cliente@ex.com", "11999");
        p.setId(id);
        return p;
    }

    private Servico servico(Long id, BigDecimal preco) {
        Servico s = new Servico();
        s.setId(id);
        s.setNome("Serviço Teste");
        s.setPreco(preco);
        return s;
    }
}
