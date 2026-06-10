package com.microbio.application.service;

import com.microbio.application.dto.*;
import com.microbio.application.exception.ResourceNotFoundException;
import com.microbio.application.model.PerguntaServico;
import com.microbio.application.model.Servico;
import com.microbio.application.repository.PerguntaServicoRepository;
import com.microbio.application.repository.ServicoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ServicoService {

    private final ServicoRepository repository;
    private final PerguntaServicoRepository perguntaRepository;
    private final AuditLogService auditLogService;

    public ServicoService(ServicoRepository repository, 
                          PerguntaServicoRepository perguntaRepository,
                          AuditLogService auditLogService) {
        this.repository = repository;
        this.perguntaRepository = perguntaRepository;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    public List<ServicoDTO> findAll() {
        return repository.findAll().stream().map(this::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public ServicoDTO findById(Long id) {
        return repository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Serviço", id));
    }

    // Usado internamente
    public Servico findEntityById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Serviço", id));
    }

    public ServicoDTO create(ServicoCreateDTO dto) {
        Servico servico = new Servico();
        servico.setNome(dto.nome());
        servico.setDescricao(dto.descricao());
        servico.setPreco(dto.preco());
        servico.setTipo(dto.tipo());
        Servico saved = repository.save(servico);

        if (dto.perguntas() != null && !dto.perguntas().isEmpty()) {
            for (String texto : dto.perguntas()) {
                if (texto != null && !texto.isBlank()) {
                    PerguntaServico p = new PerguntaServico();
                    p.setPergunta(texto.trim());
                    p.setObrigatoria(true);
                    p.setServico(saved);
                    perguntaRepository.save(p);
                }
            }
        }

        auditLogService.log("CRIACAO_SERVICO", "Servico", saved.getId().toString(),
                "Tipo de exame/serviço '" + saved.getNome() + "' criado");

        return toDTOById(saved.getId(), saved);
    }

    public ServicoDTO update(Long id, ServicoUpdateDTO dto) {
        Servico servico = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Serviço", id));
        if (dto.nome() != null) servico.setNome(dto.nome());
        if (dto.descricao() != null) servico.setDescricao(dto.descricao());
        if (dto.preco() != null) servico.setPreco(dto.preco());
        if (dto.tipo() != null) servico.setTipo(dto.tipo());
        Servico saved = repository.save(servico);

        if (dto.perguntas() != null) {
            perguntaRepository.deleteByServicoId(id);
            for (String texto : dto.perguntas()) {
                if (texto != null && !texto.isBlank()) {
                    PerguntaServico p = new PerguntaServico();
                    p.setPergunta(texto.trim());
                    p.setObrigatoria(true);
                    p.setServico(saved);
                    perguntaRepository.save(p);
                }
            }
        }

        auditLogService.log("ALTERACAO_SERVICO", "Servico", saved.getId().toString(),
                "Tipo de exame/serviço '" + saved.getNome() + "' atualizado");
        return toDTOById(id, saved);
    }

    public void delete(Long id) {
        Servico servico = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Serviço", id));
        repository.deleteById(id);
        auditLogService.log("EXCLUSAO_SERVICO", "Servico", id.toString(),
                "Tipo de exame/serviço '" + servico.getNome() + "' excluído");
    }

    private ServicoDTO toDTO(Servico s) {
        return toDTOById(s.getId(), s);
    }

    private ServicoDTO toDTOById(Long servicoId, Servico s) {
        List<PerguntaServicoDTO> perguntas = perguntaRepository.findByServicoId(servicoId)
                .stream()
                .map(p -> new PerguntaServicoDTO(p.getId(), p.getPergunta(), p.getObrigatoria(), servicoId))
                .toList();
        return new ServicoDTO(s.getId(), s.getNome(), s.getDescricao(), s.getPreco(), s.getTipo(), perguntas);
    }
}
