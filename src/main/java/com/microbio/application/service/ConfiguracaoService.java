package com.microbio.application.service;

import com.microbio.application.model.Configuracao;
import com.microbio.application.repository.ConfiguracaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ConfiguracaoService {

    private final ConfiguracaoRepository repository;
    private final AuditLogService auditLogService;

    public ConfiguracaoService(ConfiguracaoRepository repository, AuditLogService auditLogService) {
        this.repository = repository;
        this.auditLogService = auditLogService;
    }

    public String getValor(String chave, String valorPadrao) {
        return repository.findByChave(chave)
                .map(Configuracao::getValor)
                .orElse(valorPadrao);
    }

    public void salvar(String chave, String valor) {
        Configuracao config = repository.findByChave(chave)
                .orElseGet(() -> new Configuracao(chave, valor));
        
        String valorAntigo = config.getId() != null ? config.getValor() : "(novo)";
        config.setValor(valor);
        repository.save(config);

        auditLogService.log("ALTERACAO_CONFIGURACAO", "Configuracao", chave, 
                "Configuração '" + chave + "' alterada de '" + valorAntigo + "' para '" + valor + "'");
    }
}
