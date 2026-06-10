package com.microbio.application.service;

import com.microbio.application.dto.*;
import com.microbio.application.exception.BusinessException;
import com.microbio.application.exception.ResourceNotFoundException;
import com.microbio.application.model.*;
import com.microbio.application.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    private final ObservacaoOrcamentoRepository observacaoRepository;
    private final UsuarioRepository usuarioRepository;
    private final AuditLogService auditLogService;
    private final HistoricoResponsavelRepository historicoResponsavelRepository;

    public OrcamentoService(OrcamentoRepository orcamentoRepository,
                            PessoaRepository pessoaRepository,
                            ServicoRepository servicoRepository,
                            PerguntaServicoRepository perguntaRepository,
                            RespostaOrcamentoRepository respostaRepository,
                            ObservacaoOrcamentoRepository observacaoRepository,
                            UsuarioRepository usuarioRepository,
                            AuditLogService auditLogService,
                            HistoricoResponsavelRepository historicoResponsavelRepository) {
        this.orcamentoRepository = orcamentoRepository;
        this.pessoaRepository = pessoaRepository;
        this.servicoRepository = servicoRepository;
        this.perguntaRepository = perguntaRepository;
        this.respostaRepository = respostaRepository;
        this.observacaoRepository = observacaoRepository;
        this.usuarioRepository = usuarioRepository;
        this.auditLogService = auditLogService;
        this.historicoResponsavelRepository = historicoResponsavelRepository;
    }

    @Transactional(readOnly = true)
    public Page<OrcamentoDTO> findAll(Pageable pageable) {
        return orcamentoRepository.findAll(pageable).map(this::toDTO);
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
        LocalDateTime agora = LocalDateTime.now();
        orcamento.setDataCriacao(agora);
        orcamento.setDataMovimentacao(agora);
        orcamento.setStatus(dto.status() != null ? dto.status() : OrcamentoStatus.PENDENTE);
        orcamento.setObservacao(dto.observacao());
        orcamento.setValorTotal(servico.getPreco());
        orcamento.setPessoa(pessoa);
        orcamento.setServico(servico);

        Orcamento saved = orcamentoRepository.save(orcamento);
        auditLogService.log("CRIACAO_ORCAMENTO", "Orcamento", saved.getId().toString(), 
                "Orçamento #" + saved.getId() + " criado para o cliente " + pessoa.getNome() + " no valor de " + saved.getValorTotal());

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

        OrcamentoStatus statusAntigo = orcamento.getStatus();
        boolean statusAlterado = dto.status() != null && dto.status() != statusAntigo;

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

        orcamento.setDataMovimentacao(LocalDateTime.now());
        Orcamento saved = orcamentoRepository.save(orcamento);

        if (statusAlterado) {
            auditLogService.log("ALTERACAO_STATUS", "Orcamento", saved.getId().toString(),
                    "Status do orçamento #" + saved.getId() + " alterado de " + statusAntigo + " para " + saved.getStatus());
        } else {
            auditLogService.log("ALTERACAO_ORCAMENTO", "Orcamento", saved.getId().toString(),
                    "Orçamento #" + saved.getId() + " atualizado");
        }

        return toDTO(saved);
    }

    public OrcamentoDTO createByAdmin(OrcamentoCreateDTO dto) {
        Pessoa pessoa = pessoaRepository.findById(dto.pessoaId())
                .orElseThrow(() -> new ResourceNotFoundException("Pessoa", dto.pessoaId()));
        Servico servico = servicoRepository.findById(dto.servicoId())
                .orElseThrow(() -> new ResourceNotFoundException("Serviço", dto.servicoId()));

        Orcamento orcamento = new Orcamento();
        LocalDateTime agora = LocalDateTime.now();
        orcamento.setDataCriacao(agora);
        orcamento.setDataMovimentacao(agora);
        orcamento.setStatus(dto.status() != null ? dto.status() : OrcamentoStatus.PENDENTE);
        orcamento.setObservacao(dto.observacao());
        orcamento.setValorTotal(servico.getPreco());
        orcamento.setPessoa(pessoa);
        orcamento.setServico(servico);

        Orcamento saved = orcamentoRepository.save(orcamento);

        auditLogService.log("CRIACAO_ORCAMENTO", "Orcamento", saved.getId().toString(),
                "Orçamento #" + saved.getId() + " criado por administrador para o cliente " + pessoa.getNome() + " no valor de " + saved.getValorTotal());

        return toDTO(orcamentoRepository.findById(saved.getId()).orElseThrow());
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
                o.getDataMovimentacao() != null ? o.getDataMovimentacao() : o.getDataCriacao(),
                o.getStatus(),
                o.getObservacao(),
                o.getValorTotal(),
                o.getPessoa() != null ? o.getPessoa().getId() : null,
                o.getPessoa() != null ? o.getPessoa().getNome() : null,
                o.getServico() != null ? o.getServico().getId() : null,
                o.getServico() != null ? o.getServico().getNome() : null,
                o.getResponsavel() != null ? o.getResponsavel().getId() : null,
                o.getResponsavel() != null ? o.getResponsavel().getUsername() : null,
                o.getDataAtribuicao(),
                respostas
        );
    }

    @Transactional(readOnly = true)
    public List<MeusPedidosDTO> getMeusPedidos(Long pessoaId, OrcamentoStatus status) {
        return orcamentoRepository.findByPessoaIdAndStatus(pessoaId, status)
                .stream()
                .map(o -> new MeusPedidosDTO(
                        o.getId(),
                        o.getDataCriacao(),
                        o.getStatus(),
                        o.getServico() != null ? o.getServico().getNome() : null,
                        o.getValorTotal(),
                        o.getObservacao()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ObservacaoOrcamentoDTO> getObservacoes(Long orcamentoId) {
        if (!orcamentoRepository.existsById(orcamentoId)) {
            throw new ResourceNotFoundException("Orçamento", orcamentoId);
        }
        return observacaoRepository.findByOrcamentoIdOrderByDataCriacaoAsc(orcamentoId)
                .stream()
                .map(o -> new ObservacaoOrcamentoDTO(
                        o.getId(),
                        o.getTexto(),
                        o.getDataCriacao().atZone(java.time.ZoneId.of("America/Sao_Paulo")),
                        o.getUsuario().getUsername()
                ))
                .toList();
    }

    public ObservacaoOrcamentoDTO addObservacao(Long orcamentoId, ObservacaoOrcamentoCreateDTO dto, String username) {
        Orcamento orcamento = orcamentoRepository.findById(orcamentoId)
                .orElseThrow(() -> new ResourceNotFoundException("Orçamento", orcamentoId));

        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com username: " + username));

        ObservacaoOrcamento obs = new ObservacaoOrcamento();
        obs.setTexto(dto.texto());
        obs.setDataCriacao(LocalDateTime.now());
        obs.setOrcamento(orcamento);
        obs.setUsuario(usuario);

        ObservacaoOrcamento saved = observacaoRepository.save(obs);

        auditLogService.log(username, usuario.getRole(), "INCLUSAO_OBSERVACAO", "ObservacaoOrcamento", saved.getId().toString(), 
                "Observação adicionada ao orçamento #" + orcamentoId + ": " + saved.getTexto());

        return new ObservacaoOrcamentoDTO(
                saved.getId(),
                saved.getTexto(),
                saved.getDataCriacao().atZone(java.time.ZoneId.of("America/Sao_Paulo")),
                saved.getUsuario().getUsername()
        );
    }

    public OrcamentoDTO transferirResponsavel(Long orcamentoId, Long novoResponsavelId, String executorUsername) {
        Orcamento orcamento = orcamentoRepository.findById(orcamentoId)
                .orElseThrow(() -> new ResourceNotFoundException("Orçamento", orcamentoId));

        Usuario executor = usuarioRepository.findByUsername(executorUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com username: " + executorUsername));

        if (!"ADMIN_MASTER".equals(executor.getRole())) {
            throw new BusinessException("Apenas administradores Master podem transferir responsáveis.");
        }

        Usuario novoResponsavel = null;
        if (novoResponsavelId != null) {
            novoResponsavel = usuarioRepository.findById(novoResponsavelId)
                    .orElseThrow(() -> new ResourceNotFoundException("Usuário", novoResponsavelId));

            if (!"ADMIN".equals(novoResponsavel.getRole()) && !"ADMIN_MASTER".equals(novoResponsavel.getRole())) {
                throw new BusinessException("O responsável do orçamento deve ser um administrador.");
            }
        }

        Usuario responsavelAnterior = orcamento.getResponsavel();
        orcamento.setResponsavel(novoResponsavel);
        orcamento.setDataAtribuicao(novoResponsavel != null ? LocalDateTime.now() : null);

        Orcamento saved = orcamentoRepository.save(orcamento);

        // Registrar no histórico de transferências
        HistoricoResponsavel historico = new HistoricoResponsavel();
        historico.setOrcamento(saved);
        historico.setResponsavelAnterior(responsavelAnterior);
        historico.setResponsavelNovo(novoResponsavel);
        historico.setDataAlteracao(LocalDateTime.now());
        historico.setAlteradoPor(executor);
        historicoResponsavelRepository.save(historico);

        // Gravar log na auditoria
        String nomeAnterior = responsavelAnterior != null ? responsavelAnterior.getUsername() : "Nenhum";
        String nomeNovo = novoResponsavel != null ? novoResponsavel.getUsername() : "Nenhum";
        auditLogService.log(executorUsername, executor.getRole(), "TRANSFERENCIA_RESPONSAVEL", "Orcamento", saved.getId().toString(),
                "Responsável do orçamento #" + saved.getId() + " alterado de '" + nomeAnterior + "' para '" + nomeNovo + "' por " + executorUsername);

        return toDTO(saved);
    }
}
