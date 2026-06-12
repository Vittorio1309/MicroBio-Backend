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

    private static final String ENTITY_SERVICO = "Serviço";
    private static final String LOG_TIPO_SERVICO = "Tipo de exame/serviço";

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
                .orElseThrow(() -> new ResourceNotFoundException(ENTITY_SERVICO, id));
    }

    // Usado internamente
    public Servico findEntityById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ENTITY_SERVICO, id));
    }

    public ServicoDTO create(ServicoCreateDTO dto) {
        Servico servico = new Servico();
        servico.setNome(dto.nome());
        servico.setDescricao(dto.descricao());
        servico.setPreco(dto.preco());
        servico.setTipo(dto.tipo());

        if (dto.perguntas() != null) {
            for (String texto : dto.perguntas()) {
                if (texto != null && !texto.isBlank()) {
                    PerguntaServico p = new PerguntaServico();
                    p.setPergunta(texto.trim());
                    p.setObrigatoria(true);
                    p.setServico(servico);
                    servico.getPerguntas().add(p);
                }
            }
        }

        Servico saved = repository.save(servico);

        auditLogService.log("CRIACAO_SERVICO", "Servico", saved.getId().toString(),
                LOG_TIPO_SERVICO + " '" + saved.getNome() + "' criado");

        return toDTO(saved);
    }

    public ServicoDTO update(Long id, ServicoUpdateDTO dto) {
        Servico servico = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ENTITY_SERVICO, id));
        if (dto.nome() != null) servico.setNome(dto.nome());
        if (dto.descricao() != null) servico.setDescricao(dto.descricao());
        if (dto.preco() != null) servico.setPreco(dto.preco());
        if (dto.tipo() != null) servico.setTipo(dto.tipo());

        if (dto.perguntas() != null) {
            servico.getPerguntas().clear();
            for (String texto : dto.perguntas()) {
                if (texto != null && !texto.isBlank()) {
                    PerguntaServico p = new PerguntaServico();
                    p.setPergunta(texto.trim());
                    p.setObrigatoria(true);
                    p.setServico(servico);
                    servico.getPerguntas().add(p);
                }
            }
        }

        Servico saved = repository.save(servico);

        auditLogService.log("ALTERACAO_SERVICO", "Servico", saved.getId().toString(),
                LOG_TIPO_SERVICO + " '" + saved.getNome() + "' atualizado");
        return toDTO(saved);
    }

    public void delete(Long id) {
        Servico servico = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ENTITY_SERVICO, id));
        repository.deleteById(id);
        auditLogService.log("EXCLUSAO_SERVICO", "Servico", id.toString(),
                LOG_TIPO_SERVICO + " '" + servico.getNome() + "' excluído");
    }

    private ServicoDTO toDTO(Servico s) {
        List<PerguntaServicoDTO> perguntas = perguntaRepository.findByServicoId(s.getId())
                .stream()
                .map(p -> new PerguntaServicoDTO(p.getId(), p.getPergunta(), p.getObrigatoria(), s.getId()))
                .toList();
        return new ServicoDTO(s.getId(), s.getNome(), s.getDescricao(), s.getPreco(), s.getTipo(), perguntas);
    }
}
