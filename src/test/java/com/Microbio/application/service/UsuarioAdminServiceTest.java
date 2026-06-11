package com.microbio.application.service;

import com.microbio.application.dto.UsuarioCreateDTO;
import com.microbio.application.dto.UsuarioResponseDTO;
import com.microbio.application.dto.UsuarioUpdateDTO;
import com.microbio.application.exception.BusinessException;
import com.microbio.application.exception.ResourceNotFoundException;
import com.microbio.application.model.Usuario;
import com.microbio.application.repository.PessoaRepository;
import com.microbio.application.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioAdminServiceTest {

    @Mock UsuarioRepository repository;
    @Mock PessoaRepository pessoaRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock AuditLogService auditLogService;

    @InjectMocks
    UsuarioAdminService service;

    @Test
    void findAll_returnsMappedDTOs() {
        when(repository.findAll()).thenReturn(List.of(usuario(1L, "joao", "ADMIN")));

        List<UsuarioResponseDTO> result = service.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).username()).isEqualTo("joao");
        assertThat(result.get(0).role()).isEqualTo("ADMIN");
    }

    @Test
    void findByRole_returnsFilteredList() {
        when(repository.findByRole("USER")).thenReturn(List.of(usuario(1L, "ana", "USER")));

        List<UsuarioResponseDTO> result = service.findByRole("USER");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).role()).isEqualTo("USER");
    }

    @Test
    void create_encodesPasswordBeforeSaving() {
        when(repository.findByUsername("joao")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("senha123")).thenReturn("encoded_senha");
        when(repository.save(any())).thenReturn(usuario(1L, "joao", "ADMIN"));

        service.create(new UsuarioCreateDTO("joao", "senha123", "ADMIN", null));

        verify(repository).save(argThat(u -> "encoded_senha".equals(u.getSenha())));
    }

    @Test
    void create_whenRoleHasPrefix_stripsRolePrefix() {
        when(repository.findByUsername("joao")).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(repository.save(any())).thenReturn(usuario(1L, "joao", "ADMIN"));

        service.create(new UsuarioCreateDTO("joao", "senha", "ROLE_ADMIN", null));

        verify(repository).save(argThat(u -> "ADMIN".equals(u.getRole())));
    }

    @Test
    void create_whenRoleIsNull_defaultsToUser() {
        when(repository.findByUsername("maria")).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(repository.save(any())).thenReturn(usuario(1L, "maria", "USER"));

        service.create(new UsuarioCreateDTO("maria", "senha", null, null));

        verify(repository).save(argThat(u -> "USER".equals(u.getRole())));
    }

    @Test
    void create_whenUsernameAlreadyExists_throwsBusinessException() {
        when(repository.findByUsername("joao")).thenReturn(Optional.of(usuario(1L, "joao", "USER")));

        assertThatThrownBy(() -> service.create(new UsuarioCreateDTO("joao", "senha", "USER", null)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("joao");

        verify(repository, never()).save(any());
    }

    @Test
    void update_changesUsername_whenAvailable() {
        when(repository.findById(1L)).thenReturn(Optional.of(usuario(1L, "velho", "USER")));
        when(repository.findByUsername("novo")).thenReturn(Optional.empty());
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        UsuarioResponseDTO result = service.update(1L, new UsuarioUpdateDTO("novo", null));

        assertThat(result.username()).isEqualTo("novo");
    }

    @Test
    void update_whenUsernameConflictsWithAnotherUser_throwsBusinessException() {
        when(repository.findById(1L)).thenReturn(Optional.of(usuario(1L, "atual", "USER")));
        when(repository.findByUsername("conflito")).thenReturn(Optional.of(usuario(2L, "conflito", "USER")));

        assertThatThrownBy(() -> service.update(1L, new UsuarioUpdateDTO("conflito", null)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("conflito");
    }

    @Test
    void update_encodesNewPassword_whenProvided() {
        when(repository.findById(1L)).thenReturn(Optional.of(usuario(1L, "joao", "USER")));
        when(passwordEncoder.encode("nova_senha")).thenReturn("nova_encoded");
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.update(1L, new UsuarioUpdateDTO(null, "nova_senha"));

        verify(repository).save(argThat(u -> "nova_encoded".equals(u.getSenha())));
    }

    @Test
    void update_whenNotFound_throwsResourceNotFoundException() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(99L, new UsuarioUpdateDTO("x", null)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void delete_whenExists_deletesAndAudits() {
        when(repository.findById(1L)).thenReturn(Optional.of(usuario(1L, "joao", "USER")));

        service.delete(1L);

        verify(repository).deleteById(1L);
        verify(auditLogService).log(eq("EXCLUSAO_USUARIO"), eq("Usuario"), eq("1"), anyString());
    }

    @Test
    void delete_whenNotFound_throwsResourceNotFoundException() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(repository, never()).deleteById(any());
    }

    private Usuario usuario(Long id, String username, String role) {
        Usuario u = new Usuario(username, "encoded", role);
        u.setId(id);
        return u;
    }
}
