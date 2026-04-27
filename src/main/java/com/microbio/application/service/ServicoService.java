package com.microbio.application.service;

import com.microbio.application.model.Servico;
import com.microbio.application.repository.ServicoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ServicoService {

    private final ServicoRepository repository;

    public ServicoService(ServicoRepository repository) {
        this.repository = repository;
    }

    public List<Servico> findAll() {
        return repository.findAll();
    }

    public Optional<Servico> findById(Long id) {
        return repository.findById(id);
    }

    public Servico save(Servico servico) {
        return repository.save(servico);
    }

    public void deleteById(Long id) {
        repository.deleteById(id);
    }
}
