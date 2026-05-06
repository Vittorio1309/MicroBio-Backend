package com.microbio.application.service;

import com.microbio.application.dto.*;
import com.microbio.application.exception.BusinessException;
import com.microbio.application.exception.ResourceNotFoundException;
import com.microbio.application.model.*;
import com.microbio.application.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class OrcamentoService {

    private final OrcamentoRepository orcamentoRepository;
    private final PessoaRepository pessoaRepository;
    private final ServicoRepository servicoRepository;
    private final PerguntaServicoRepository perguntaRepository;
    private final RespostaOrcamentoRepository respostaRepository;

    public OrcamentoService(OrcamentoRepository orcamentoRepository,
                            PessoaRepository pessoaRepository,
                            ServicoRepository servicoRepository,
                            PerguntaServicoRepository perguntaRepository,
                            RespostaOrcamentoRepository respostaRepository) {
        this.orcamentoRepository = orcamentoRepository;
        this.pessoaRepository = pessoaRepository;
        this.servicoRepository = servicoRepository;
        this.perguntaRepository = perguntaRepository;
        this.respostaRepository = respostaRepository;
    }

    @Transactional(readOnly = true)
    public List<OrcamentoDTO> findAll() {
        return orcamentoRepository.findAll().stream().map(this::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public OrcamentoDTO findById(Long id) {
        return orcamentoRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Orçamento", id));
    }

    @Transactional(readOnly = true)
    public List<OrcamentoDTO> findByPessoa(Long pessoaId) {
        return orcamentoRepository.findByPessoaId(pessoaId).stream().map(this::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public List<OrcamentoDTO> findByStatus(OrcamentoStatus status) {
        return orcamentoRepository.findByStatus(status).stream().map(this::toDTO).toList();
    }

    public OrcamentoDTO create(OrcamentoCreateDTO dto) {
        Pessoa pessoa = pessoaRepository.findById(dto.pessoaId())
                .orElseThrow(() -> new ResourceNotFoundException("Pessoa", dto.pessoaId()));

        Servico servico = servicoRepository.findById(dto.servicoId())
                .orElseThrow(() -> new ResourceNotFoundException("Serviço", dto.servicoId()));

        // Validar respostas obrigatórias
        List<PerguntaServico> perguntasObrigatorias = perguntaRepository
                .findByServicoId(dto.servicoId())
                .stream()
                .filter(PerguntaServico::getObrigatoria)
                .toList();

        List<Long> perguntasRespondidas = dto.respostas() != null
                ? dto.respostas().stream().map(RespostaDTO::perguntaId).toList()
                : List.of();

        for (PerguntaServico pergunta : perguntasObrigatorias) {
            if (!perguntasRespondidas.contains(pergunta.getId())) {
                throw new BusinessException("Pergunta obrigatória não respondida: " + pergunta.getPergunta());
            }
        }

        Orcamento orcamento = new Orcamento();
        orcamento.setDataCriacao(LocalDateTime.now());
        orcamento.setStatus(dto.status() != null ? dto.status() : OrcamentoStatus.PENDENTE);
        orcamento.setObservacao(dto.observacao());
        orcamento.setValorTotal(servico.getPreco());
        orcamento.setPessoa(pessoa);
        orcamento.setServico(servico);

        Orcamento saved = orcamentoRepository.save(orcamento);

        // Salvar respostas
        if (dto.respostas() != null) {
            for (RespostaDTO respostaDTO : dto.respostas()) {
                PerguntaServico pergunta = perguntaRepository.findById(respostaDTO.perguntaId())
                        .orElseThrow(() -> new ResourceNotFoundException("Pergunta", respostaDTO.perguntaId()));

                RespostaOrcamento resposta = new RespostaOrcamento();
                resposta.setPergunta(pergunta);
                resposta.setOrcamento(saved);
                resposta.setResposta(respostaDTO.resposta());
                respostaRepository.save(resposta);
            }
        }

        return toDTO(orcamentoRepository.findById(saved.getId()).orElseThrow());
    }

    public OrcamentoDTO updateStatus(Long id, OrcamentoUpdateDTO dto) {
        Orcamento orcamento = orcamentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Orçamento", id));

        if (dto.status() != null) orcamento.setStatus(dto.status());
        if (dto.observacao() != null) orcamento.setObservacao(dto.observacao());

        if (dto.pessoaId() != null) {
            Pessoa pessoa = pessoaRepository.findById(dto.pessoaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Pessoa", dto.pessoaId()));
            orcamento.setPessoa(pessoa);
        }

        if (dto.servicoId() != null) {
            Servico servico = servicoRepository.findById(dto.servicoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Serviço", dto.servicoId()));
            orcamento.setServico(servico);
        }

        return toDTO(orcamentoRepository.save(orcamento));
    }

    public void delete(Long id) {
        if (!orcamentoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Orçamento", id);
        }
        orcamentoRepository.deleteById(id);
    }

    private OrcamentoDTO toDTO(Orcamento o) {
        List<RespostaDTO> respostas = respostaRepository.findByOrcamentoId(o.getId())
                .stream()
                .map(r -> new RespostaDTO(
                        r.getPergunta().getId(),
                        r.getPergunta().getPergunta(),
                        r.getResposta()))
                .toList();

        return new OrcamentoDTO(
                o.getId(),
                o.getDataCriacao(),
                o.getStatus(),
                o.getObservacao(),
                o.getValorTotal(),
                o.getPessoa() != null ? o.getPessoa().getId() : null,
                o.getPessoa() != null ? o.getPessoa().getNome() : null,
                o.getServico() != null ? o.getServico().getId() : null,
                o.getServico() != null ? o.getServico().getNome() : null,
                respostas
        );
    }

    @Transactional(readOnly = true)
    public List<MeusPedidosDTO> getMeusPedidos(Long pessoaId, OrcamentoStatus status) {
        List<OrcamentoDTO> orcamentos = orcamentoRepository
                .findByPessoaIdAndStatus(pessoaId, status)
                .stream()
                .map(this::toDTO)
                .toList();

        return orcamentos.stream()
                .map(o -> new MeusPedidosDTO(
                        o.id(),
                        o.dataCriacao(),
                        o.status(),
                        o.servicoNome(),
                        o.valorTotal(),
                        o.observacao()
                ))
                .toList();
    }
}
