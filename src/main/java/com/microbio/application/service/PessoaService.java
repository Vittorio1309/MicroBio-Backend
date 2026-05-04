package com.microbio.application.service;

import com.microbio.application.dto.PessoaCreateDTO;
import com.microbio.application.dto.PessoaDTO;
import com.microbio.application.dto.PessoaUpdate;
import com.microbio.application.exception.BusinessException;
import com.microbio.application.exception.ResourceNotFoundException;
import com.microbio.application.model.Pessoa;
import com.microbio.application.repository.PessoaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class PessoaService {

    private final PessoaRepository repository;

    public PessoaService(PessoaRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<PessoaDTO> findAll() {
        return repository.findAll().stream().map(this::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public PessoaDTO findById(Long id) {
        return repository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Pessoa", id));
    }

    public PessoaDTO create(PessoaCreateDTO dto) {
        if (repository.existsByEmail(dto.email())) {
            throw new BusinessException("Já existe uma pessoa cadastrada com o email: " + dto.email());
        }
        Pessoa pessoa = new Pessoa(dto.nome(), dto.email(), dto.telefone());
        return toDTO(repository.save(pessoa));
    }

    public PessoaDTO update(Long id, PessoaUpdate dto) {
        Pessoa pessoa = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pessoa", id));

        if (dto.email() != null && !dto.email().equals(pessoa.getEmail()) && repository.existsByEmail(dto.email())) {
            throw new BusinessException("Já existe uma pessoa cadastrada com o email: " + dto.email());
        }

        if (dto.nome() != null) pessoa.setNome(dto.nome());
        if (dto.email() != null) pessoa.setEmail(dto.email());
        if (dto.telefone() != null) pessoa.setTelefone(dto.telefone());

        return toDTO(repository.save(pessoa));
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Pessoa", id);
        }
        repository.deleteById(id);
    }

    private PessoaDTO toDTO(Pessoa p) {
        return new PessoaDTO(p.getId(), p.getNome(), p.getEmail(), p.getTelefone());
    }
}
