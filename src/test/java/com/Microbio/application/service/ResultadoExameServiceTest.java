package com.microbio.application.service;

import com.microbio.application.dto.ResultadoExameCreateDTO;
import com.microbio.application.dto.ResultadoExameDTO;
import com.microbio.application.dto.ResultadoExameUpdateDTO;
import com.microbio.application.exception.BusinessException;
import com.microbio.application.exception.ResourceNotFoundException;
import com.microbio.application.model.ResultadoExame;
import com.microbio.application.model.ResultadoExameStatus;
import com.microbio.application.model.Usuario;
import com.microbio.application.repository.ResultadoExameRepository;
import com.microbio.application.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResultadoExameServiceTest {

    @TempDir
    Path tempDir;

    @Mock ResultadoExameRepository repository;
    @Mock UsuarioRepository usuarioRepository;
    @Mock AuditLogService auditLogService;

    ResultadoExameService service;

    @BeforeEach
    void setUp() {
        service = new ResultadoExameService(repository, usuarioRepository, auditLogService, tempDir.toString());
    }

    @Test
    void findAll_returnsMappedDTOs() {
        Usuario u = usuario(1L, "user1", "USER");
        when(repository.findAll()).thenReturn(List.of(resultado(1L, ResultadoExameStatus.PENDENTE, u)));

        List<ResultadoExameDTO> result = service.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(1L);
        assertThat(result.get(0).status()).isEqualTo(ResultadoExameStatus.PENDENTE);
    }

    @Test
    void findById_whenNotFound_throwsResourceNotFoundException() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void create_setsStatusPendente_andLinksToUsuario() {
        Usuario u = usuario(1L, "user1", "USER");
        ResultadoExame saved = resultado(1L, ResultadoExameStatus.PENDENTE, u);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(u));
        when(repository.save(any())).thenReturn(saved);

        ResultadoExameDTO result = service.create(new ResultadoExameCreateDTO(1L, "Desc", "Laudo", null));

        assertThat(result.status()).isEqualTo(ResultadoExameStatus.PENDENTE);
        assertThat(result.username()).isEqualTo("user1");
        verify(repository).save(argThat(r ->
                r.getStatus() == ResultadoExameStatus.PENDENTE && r.getUsuario().equals(u)));
    }

    @Test
    void create_whenUsuarioNotFound_throwsResourceNotFoundException() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(new ResultadoExameCreateDTO(99L, "Desc", null, null)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void update_changesFieldsAndAudits() {
        Usuario u = usuario(1L, "user1", "USER");
        ResultadoExame existing = resultado(1L, ResultadoExameStatus.PENDENTE, u);
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ResultadoExameDTO result = service.update(1L,
                new ResultadoExameUpdateDTO("Nova desc", null, null, ResultadoExameStatus.EM_ANDAMENTO));

        assertThat(result.descricao()).isEqualTo("Nova desc");
        assertThat(result.status()).isEqualTo(ResultadoExameStatus.EM_ANDAMENTO);
        verify(auditLogService).log(eq("ALTERACAO_RESULTADO_EXAME"), eq("ResultadoExame"), eq("1"), anyString());
    }

    @Test
    void visualizar_whenOwner_updatesStatusToVisualizado() {
        Usuario u = usuario(1L, "user1", "USER");
        ResultadoExame r = resultado(1L, ResultadoExameStatus.PENDENTE, u);
        when(repository.findById(1L)).thenReturn(Optional.of(r));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ResultadoExameDTO result = service.visualizar(1L, "user1");

        assertThat(result.status()).isEqualTo(ResultadoExameStatus.VISUALIZADO);
        verify(repository).save(argThat(re -> re.getStatus() == ResultadoExameStatus.VISUALIZADO));
    }

    @Test
    void visualizar_whenStatusEmAndamento_updatesStatusToVisualizado() {
        Usuario u = usuario(1L, "user1", "USER");
        ResultadoExame r = resultado(1L, ResultadoExameStatus.EM_ANDAMENTO, u);
        when(repository.findById(1L)).thenReturn(Optional.of(r));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ResultadoExameDTO result = service.visualizar(1L, "user1");

        assertThat(result.status()).isEqualTo(ResultadoExameStatus.VISUALIZADO);
    }

    @Test
    void visualizar_whenStatusFinalizado_doesNotChangeStatus() {
        Usuario u = usuario(1L, "user1", "USER");
        ResultadoExame r = resultado(1L, ResultadoExameStatus.FINALIZADO, u);
        when(repository.findById(1L)).thenReturn(Optional.of(r));

        ResultadoExameDTO result = service.visualizar(1L, "user1");

        assertThat(result.status()).isEqualTo(ResultadoExameStatus.FINALIZADO);
        verify(repository, never()).save(any());
    }

    @Test
    void visualizar_whenNotOwner_throwsBusinessException() {
        Usuario u = usuario(1L, "user1", "USER");
        ResultadoExame r = resultado(1L, ResultadoExameStatus.PENDENTE, u);
        when(repository.findById(1L)).thenReturn(Optional.of(r));

        assertThatThrownBy(() -> service.visualizar(1L, "outro_user"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Acesso negado");
    }

    @Test
    void downloadArquivo_whenArquivoIsNull_throwsBusinessException() {
        Usuario u = usuario(1L, "user1", "USER");
        ResultadoExame r = resultado(1L, ResultadoExameStatus.PENDENTE, u);
        r.setArquivoUrl(null);
        when(repository.findById(1L)).thenReturn(Optional.of(r));

        assertThatThrownBy(() -> service.downloadArquivo(1L, "user1"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("não possui arquivo");
    }

    @Test
    void downloadArquivo_whenArquivoIsHttpUrl_throwsBusinessException() {
        Usuario u = usuario(1L, "user1", "USER");
        ResultadoExame r = resultado(1L, ResultadoExameStatus.PENDENTE, u);
        r.setArquivoUrl("http://external.com/file.pdf");
        when(repository.findById(1L)).thenReturn(Optional.of(r));

        assertThatThrownBy(() -> service.downloadArquivo(1L, "user1"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("não possui arquivo");
    }

    @Test
    void downloadArquivo_whenNeitherOwnerNorAdmin_throwsBusinessException() {
        Usuario owner = usuario(1L, "owner", "USER");
        Usuario stranger = usuario(2L, "estranho", "USER");
        ResultadoExame r = resultado(1L, ResultadoExameStatus.PENDENTE, owner);
        r.setArquivoUrl("file.pdf");
        when(repository.findById(1L)).thenReturn(Optional.of(r));
        when(usuarioRepository.findByUsername("estranho")).thenReturn(Optional.of(stranger));

        assertThatThrownBy(() -> service.downloadArquivo(1L, "estranho"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Acesso negado");
    }

    @Test
    void delete_whenExists_deletesAndAudits() {
        Usuario u = usuario(1L, "user1", "USER");
        ResultadoExame r = resultado(1L, ResultadoExameStatus.PENDENTE, u);
        r.setArquivoUrl(null);
        when(repository.findById(1L)).thenReturn(Optional.of(r));

        service.delete(1L);

        verify(repository).deleteById(1L);
        verify(auditLogService).log(eq("EXCLUSAO_RESULTADO_EXAME"), eq("ResultadoExame"), eq("1"), anyString());
    }

    @Test
    void delete_whenNotFound_throwsResourceNotFoundException() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    private Usuario usuario(Long id, String username, String role) {
        Usuario u = new Usuario(username, "encoded", role);
        u.setId(id);
        return u;
    }

    private ResultadoExame resultado(Long id, ResultadoExameStatus status, Usuario usuario) {
        ResultadoExame r = new ResultadoExame();
        r.setId(id);
        r.setDescricao("Teste");
        r.setDataEmissao(LocalDateTime.now());
        r.setStatus(status);
        r.setUsuario(usuario);
        return r;
    }
}
