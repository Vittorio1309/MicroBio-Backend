package com.microbio.application.service;

import com.microbio.application.dto.ResultadoExameCreateDTO;
import com.microbio.application.dto.ResultadoExameDTO;
import com.microbio.application.dto.ResultadoExameUpdateDTO;
import com.microbio.application.exception.BusinessException;
import com.microbio.application.exception.ResourceNotFoundException;
import com.microbio.application.model.Orcamento;
import com.microbio.application.model.ResultadoExame;
import com.microbio.application.repository.OrcamentoRepository;
import com.microbio.application.repository.ResultadoExameRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class ResultadoExameService {

    private final ResultadoExameRepository repository;
    private final OrcamentoRepository orcamentoRepository;

    public ResultadoExameService(ResultadoExameRepository repository, OrcamentoRepository orcamentoRepository) {
        this.repository = repository;
        this.orcamentoRepository = orcamentoRepository;
    }

    @Transactional(readOnly = true)
    public List<ResultadoExameDTO> findAll() {
        return repository.findAll().stream().map(this::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public ResultadoExameDTO findById(Long id) {
        return repository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Resultado de Exame", id));
    }

    @Transactional(readOnly = true)
    public List<ResultadoExameDTO> findByOrcamento(Long orcamentoId) {
        return repository.findByOrcamentoId(orcamentoId).stream().map(this::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public List<ResultadoExameDTO> findByPessoa(Long pessoaId) {
        return repository.findByOrcamentoPessoaId(pessoaId).stream().map(this::toDTO).toList();
    }

    public ResultadoExameDTO create(ResultadoExameCreateDTO dto) {
        Orcamento orcamento = orcamentoRepository
                .findFirstByPessoaIdAndServicoIdOrderByDataCriacaoDesc(dto.pessoaId(), dto.servicoId())
                .orElseThrow(() -> new BusinessException(
                        "Nenhum orçamento encontrado para o cliente " + dto.pessoaId()
                        + " e serviço " + dto.servicoId()));

        ResultadoExame resultado = new ResultadoExame();
        resultado.setDescricao(dto.descricao());
        resultado.setDataEmissao(LocalDateTime.now());
        resultado.setLaudo(dto.laudo());
        resultado.setArquivoUrl(dto.arquivoUrl());
        resultado.setOrcamento(orcamento);

        return toDTO(repository.save(resultado));
    }

    public ResultadoExameDTO update(Long id, ResultadoExameUpdateDTO dto) {
        ResultadoExame resultado = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resultado de Exame", id));

        if (dto.descricao() != null) resultado.setDescricao(dto.descricao());
        if (dto.laudo() != null) resultado.setLaudo(dto.laudo());
        if (dto.arquivoUrl() != null) resultado.setArquivoUrl(dto.arquivoUrl());

        return toDTO(repository.save(resultado));
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Resultado de Exame", id);
        }
        repository.deleteById(id);
    }

    private ResultadoExameDTO toDTO(ResultadoExame r) {
        return new ResultadoExameDTO(
                r.getId(),
                r.getDescricao(),
                r.getDataEmissao(),
                r.getLaudo(),
                r.getArquivoUrl(),
                r.getOrcamento().getId()
        );
    }
}
