package com.microbio.application.service;

import com.microbio.application.dto.PessoaCreateDTO;
import com.microbio.application.dto.PessoaDTO;
import com.microbio.application.dto.PessoaUpdate;
import com.microbio.application.exception.BusinessException;
import com.microbio.application.exception.ResourceNotFoundException;
import com.microbio.application.model.Pessoa;
import com.microbio.application.repository.PessoaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PessoaServiceTest {

    @Mock
    private PessoaRepository repository;

    @InjectMocks
    private PessoaService service;

    @Test
    void findAll_returnsMappedDTOs() {
        Pessoa p = pessoa(1L, "Maria", "maria@ex.com", "11999");
        when(repository.findAll()).thenReturn(List.of(p));

        List<PessoaDTO> result = service.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).nome()).isEqualTo("Maria");
        assertThat(result.get(0).email()).isEqualTo("maria@ex.com");
    }

    @Test
    void findById_whenExists_returnsDTO() {
        when(repository.findById(1L)).thenReturn(Optional.of(pessoa(1L, "João", "joao@ex.com", "21999")));

        PessoaDTO result = service.findById(1L);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.nome()).isEqualTo("João");
    }

    @Test
    void findById_whenNotFound_throwsResourceNotFoundException() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void create_whenEmailIsUnique_createsPessoa() {
        PessoaCreateDTO dto = new PessoaCreateDTO("Ana", "ana@ex.com", "31999");
        when(repository.existsByEmail(dto.email())).thenReturn(false);
        when(repository.save(any())).thenReturn(pessoa(2L, "Ana", "ana@ex.com", "31999"));

        PessoaDTO result = service.create(dto);

        assertThat(result.id()).isEqualTo(2L);
        assertThat(result.email()).isEqualTo("ana@ex.com");
        verify(repository).save(any(Pessoa.class));
    }

    @Test
    void create_whenEmailDuplicated_throwsBusinessException() {
        PessoaCreateDTO dto = new PessoaCreateDTO("Ana", "dup@ex.com", "31999");
        when(repository.existsByEmail(dto.email())).thenReturn(true);

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("dup@ex.com");

        verify(repository, never()).save(any());
    }

    @Test
    void update_whenExists_updatesFields() {
        Pessoa existing = pessoa(1L, "Velho Nome", "old@ex.com", "00000");
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.existsByEmail("new@ex.com")).thenReturn(false);
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        PessoaUpdate dto = new PessoaUpdate("Novo Nome", "new@ex.com", "99999");
        PessoaDTO result = service.update(1L, dto);

        assertThat(result.nome()).isEqualTo("Novo Nome");
        assertThat(result.email()).isEqualTo("new@ex.com");
    }

    @Test
    void update_whenNotFound_throwsResourceNotFoundException() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(99L, new PessoaUpdate("X", null, null)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void delete_whenExists_deletesSuccessfully() {
        when(repository.existsById(1L)).thenReturn(true);

        service.delete(1L);

        verify(repository).deleteById(1L);
    }

    @Test
    void delete_whenNotFound_throwsResourceNotFoundException() {
        when(repository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> service.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(repository, never()).deleteById(any());
    }

    private Pessoa pessoa(Long id, String nome, String email, String telefone) {
        Pessoa p = new Pessoa(nome, email, telefone);
        p.setId(id);
        return p;
    }
}
