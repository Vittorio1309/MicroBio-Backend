package com.microbio.application.service;

import com.microbio.application.model.Configuracao;
import com.microbio.application.repository.ConfiguracaoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConfiguracaoServiceTest {

    @Mock
    private ConfiguracaoRepository repository;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private ConfiguracaoService service;

    @Test
    void getValor_whenExists_returnsValue() {
        Configuracao config = new Configuracao("chave_teste", "valor_teste");
        when(repository.findByChave("chave_teste")).thenReturn(Optional.of(config));

        String result = service.getValor("chave_teste", "padrao");

        assertThat(result).isEqualTo("valor_teste");
    }

    @Test
    void getValor_whenNotFound_returnsDefault() {
        when(repository.findByChave("chave_teste")).thenReturn(Optional.empty());

        String result = service.getValor("chave_teste", "padrao");

        assertThat(result).isEqualTo("padrao");
    }

    @Test
    void salvar_createsNewConfig_andLogs() {
        when(repository.findByChave("nova_chave")).thenReturn(Optional.empty());

        service.salvar("nova_chave", "novo_valor");

        verify(repository).save(any(Configuracao.class));
        verify(auditLogService).log(eq("ALTERACAO_CONFIGURACAO"), eq("Configuracao"), eq("nova_chave"), anyString());
    }

    @Test
    void salvar_updatesExistingConfig_andLogs() {
        Configuracao existing = new Configuracao("chave_existente", "valor_antigo");
        existing.setId(1L);
        when(repository.findByChave("chave_existente")).thenReturn(Optional.of(existing));

        service.salvar("chave_existente", "valor_novo");

        assertThat(existing.getValor()).isEqualTo("valor_novo");
        verify(repository).save(existing);
        verify(auditLogService).log(eq("ALTERACAO_CONFIGURACAO"), eq("Configuracao"), eq("chave_existente"), anyString());
    }
}
