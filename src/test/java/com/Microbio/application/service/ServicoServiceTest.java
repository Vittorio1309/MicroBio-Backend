package com.microbio.application.service;

import com.microbio.application.dto.ServicoCreateDTO;
import com.microbio.application.dto.ServicoDTO;
import com.microbio.application.dto.ServicoUpdateDTO;
import com.microbio.application.exception.ResourceNotFoundException;
import com.microbio.application.model.PerguntaServico;
import com.microbio.application.model.Servico;
import com.microbio.application.repository.PerguntaServicoRepository;
import com.microbio.application.repository.ServicoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServicoServiceTest {

    @Mock ServicoRepository repository;
    @Mock PerguntaServicoRepository perguntaRepository;
    @Mock AuditLogService auditLogService;

    @InjectMocks
    ServicoService service;

    @Test
    void findAll_returnsMappedDTOs() {
        when(repository.findAll()).thenReturn(List.of(servico(1L, "Análise de Água")));
        when(perguntaRepository.findByServicoId(1L)).thenReturn(List.of());

        List<ServicoDTO> result = service.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).nome()).isEqualTo("Análise de Água");
    }

    @Test
    void findById_whenExists_returnsDTO() {
        when(repository.findById(1L)).thenReturn(Optional.of(servico(1L, "Solo")));
        when(perguntaRepository.findByServicoId(1L)).thenReturn(List.of());

        ServicoDTO result = service.findById(1L);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.nome()).isEqualTo("Solo");
    }

    @Test
    void findById_whenNotFound_throwsResourceNotFoundException() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void create_withoutPerguntas_savesServico() {
        Servico saved = servico(1L, "Água");
        when(repository.save(any())).thenReturn(saved);
        when(perguntaRepository.findByServicoId(1L)).thenReturn(List.of());

        ServicoDTO result = service.create(new ServicoCreateDTO("Água", "Desc", BigDecimal.valueOf(200), "AGUA", null));

        assertThat(result.id()).isEqualTo(1L);
        verify(repository).save(any(Servico.class));
    }

    @Test
    void create_withPerguntas_attachesQuestionsToServico() {
        Servico saved = servico(1L, "Água");
        when(repository.save(any())).thenReturn(saved);
        when(perguntaRepository.findByServicoId(1L)).thenReturn(List.of());

        service.create(new ServicoCreateDTO("Água", "Desc", BigDecimal.valueOf(200), "AGUA", List.of("Fonte?", "pH?")));

        verify(repository).save(argThat(s -> s.getPerguntas().size() == 2));
    }

    @Test
    void create_blanksInPerguntasList_areIgnored() {
        Servico saved = servico(1L, "Solo");
        when(repository.save(any())).thenReturn(saved);
        when(perguntaRepository.findByServicoId(1L)).thenReturn(List.of());

        service.create(new ServicoCreateDTO("Solo", null, BigDecimal.valueOf(100), "SOLO", List.of("  ", "Textura?")));

        verify(repository).save(argThat(s -> s.getPerguntas().size() == 1));
    }

    @Test
    void update_whenNotFound_throwsResourceNotFoundException() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(99L, new ServicoUpdateDTO("X", null, null, null, null)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void update_replacesPerguntas_whenListProvided() {
        Servico existing = servico(1L, "Água");
        PerguntaServico antiga = new PerguntaServico();
        antiga.setPergunta("Antiga?");
        antiga.setObrigatoria(true);
        antiga.setServico(existing);
        existing.getPerguntas().add(antiga);

        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(perguntaRepository.findByServicoId(1L)).thenReturn(List.of());

        service.update(1L, new ServicoUpdateDTO(null, null, null, null, List.of("Nova pergunta?")));

        verify(repository).save(argThat(s ->
                s.getPerguntas().size() == 1 && "Nova pergunta?".equals(s.getPerguntas().get(0).getPergunta())));
    }

    @Test
    void update_auditsChange() {
        when(repository.findById(1L)).thenReturn(Optional.of(servico(1L, "Água")));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(perguntaRepository.findByServicoId(1L)).thenReturn(List.of());

        service.update(1L, new ServicoUpdateDTO("Água Atualizado", null, null, null, null));

        verify(auditLogService).log(eq("ALTERACAO_SERVICO"), eq("Servico"), eq("1"), anyString());
    }

    @Test
    void delete_whenExists_deletesAndAudits() {
        when(repository.findById(1L)).thenReturn(Optional.of(servico(1L, "Água")));

        service.delete(1L);

        verify(repository).deleteById(1L);
        verify(auditLogService).log(eq("EXCLUSAO_SERVICO"), eq("Servico"), eq("1"), anyString());
    }

    @Test
    void delete_whenNotFound_throwsResourceNotFoundException() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(repository, never()).deleteById(any());
    }

    private Servico servico(Long id, String nome) {
        Servico s = new Servico();
        s.setId(id);
        s.setNome(nome);
        s.setPreco(BigDecimal.valueOf(100));
        return s;
    }
}
