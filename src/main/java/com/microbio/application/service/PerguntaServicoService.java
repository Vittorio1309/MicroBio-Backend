package com.microbio.application.service;

import com.microbio.application.dto.PerguntaServicoCreateDTO;
import com.microbio.application.dto.PerguntaServicoDTO;
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
public class PerguntaServicoService {

    private final PerguntaServicoRepository repository;
    private final ServicoRepository servicoRepository;

    public PerguntaServicoService(PerguntaServicoRepository repository, ServicoRepository servicoRepository) {
        this.repository = repository;
        this.servicoRepository = servicoRepository;
    }

    @Transactional(readOnly = true)
    public List<PerguntaServicoDTO> findByServico(Long servicoId) {
        if (!servicoRepository.existsById(servicoId)) {
            throw new ResourceNotFoundException("Serviço", servicoId);
        }
        return repository.findByServicoId(servicoId).stream().map(this::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public PerguntaServicoDTO findById(Long id) {
        return repository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Pergunta", id));
    }

    public PerguntaServicoDTO create(PerguntaServicoCreateDTO dto) {
        Servico servico = servicoRepository.findById(dto.servicoId())
                .orElseThrow(() -> new ResourceNotFoundException("Serviço", dto.servicoId()));

        PerguntaServico pergunta = new PerguntaServico();
        pergunta.setPergunta(dto.pergunta());
        pergunta.setObrigatoria(dto.obrigatoria() != null ? dto.obrigatoria() : true);
        pergunta.setServico(servico);

        return toDTO(repository.save(pergunta));
    }

    public PerguntaServicoDTO update(Long id, PerguntaServicoCreateDTO dto) {
        PerguntaServico pergunta = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pergunta", id));

        if (dto.pergunta() != null) pergunta.setPergunta(dto.pergunta());
        if (dto.obrigatoria() != null) pergunta.setObrigatoria(dto.obrigatoria());

        return toDTO(repository.save(pergunta));
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Pergunta", id);
        }
        repository.deleteById(id);
    }

    private PerguntaServicoDTO toDTO(PerguntaServico p) {
        return new PerguntaServicoDTO(p.getId(), p.getPergunta(), p.getObrigatoria(), p.getServico().getId());
    }
}
