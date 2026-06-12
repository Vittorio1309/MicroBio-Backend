package com.microbio.application.service;

import com.microbio.application.dto.PessoaCreateDTO;
import com.microbio.application.dto.PessoaDTO;
import com.microbio.application.dto.PessoaUpdate;
import com.microbio.application.exception.BusinessException;
import com.microbio.application.exception.ResourceNotFoundException;
import com.microbio.application.model.Pessoa;
import com.microbio.application.repository.OrcamentoRepository;
import com.microbio.application.repository.PessoaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class PessoaService {

    private static final String ENTITY_PESSOA = "Pessoa";

    private final PessoaRepository repository;
    private final OrcamentoRepository orcamentoRepository;
    private final AuditLogService auditLogService;

    public PessoaService(PessoaRepository repository,
                         OrcamentoRepository orcamentoRepository,
                         AuditLogService auditLogService) {
        this.repository = repository;
        this.orcamentoRepository = orcamentoRepository;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    public List<PessoaDTO> findAll() {
        return repository.findAll().stream().map(this::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public PessoaDTO findById(Long id) {
        return repository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException(ENTITY_PESSOA, id));
    }

    public PessoaDTO create(PessoaCreateDTO dto) {
        if (repository.existsByEmail(dto.email())) {
            throw new BusinessException("Já existe um contato cadastrado com o email: " + dto.email());
        }

        Pessoa pessoa = new Pessoa(dto.nome(), dto.email(), dto.telefone());
        Pessoa saved = repository.save(pessoa);
        auditLogService.log("CRIACAO_CLIENTE", ENTITY_PESSOA, saved.getId().toString(),
                "Cliente '" + saved.getNome() + "' cadastrado");
        return toDTO(saved);
    }

    public PessoaDTO update(Long id, PessoaUpdate dto) {
        Pessoa pessoa = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ENTITY_PESSOA, id));

        if (dto.email() != null && !dto.email().equals(pessoa.getEmail()) && repository.existsByEmail(dto.email())) {
            throw new BusinessException("Já existe um contato cadastrado com o email: " + dto.email());
        }

        if (dto.nome() != null) pessoa.setNome(dto.nome());
        if (dto.email() != null) pessoa.setEmail(dto.email());
        if (dto.telefone() != null) pessoa.setTelefone(dto.telefone());

        Pessoa saved = repository.save(pessoa);
        auditLogService.log("ALTERACAO_CLIENTE", ENTITY_PESSOA, saved.getId().toString(),
                "Cliente '" + saved.getNome() + "' atualizado");
        return toDTO(saved);
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException(ENTITY_PESSOA, id);
        }
        repository.deleteById(id);
        auditLogService.log("EXCLUSAO_CLIENTE", ENTITY_PESSOA, id.toString(),
                "Cliente ID " + id + " excluído");
    }

    private PessoaDTO toDTO(Pessoa p) {
        String status = orcamentoRepository.findFirstByPessoaIdOrderByDataCriacaoDesc(p.getId())
                .map(o -> o.getStatus().name())
                .orElse(null);
        return new PessoaDTO(p.getId(), p.getNome(), p.getEmail(), p.getTelefone(), status);
    }
}
