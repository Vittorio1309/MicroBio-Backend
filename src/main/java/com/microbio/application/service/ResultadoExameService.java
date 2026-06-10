package com.microbio.application.service;

import com.microbio.application.dto.ResultadoExameCreateDTO;
import com.microbio.application.dto.ResultadoExameDTO;
import com.microbio.application.dto.ResultadoExameUpdateDTO;
import com.microbio.application.exception.BusinessException;
import com.microbio.application.exception.ResourceNotFoundException;
import com.microbio.application.model.ResultadoExame;
import com.microbio.application.model.ResultadoExameStatus;
import com.microbio.application.model.Usuario;
import com.microbio.application.repository.ResultadoExameRepository;
import com.microbio.application.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ResultadoExameService {

    private final ResultadoExameRepository repository;
    private final UsuarioRepository usuarioRepository;
    private final AuditLogService auditLogService;
    private final Path uploadsDir;

    public ResultadoExameService(ResultadoExameRepository repository,
                                 UsuarioRepository usuarioRepository,
                                 AuditLogService auditLogService,
                                 @Value("${app.uploads.dir:uploads}") String uploadsDir) {
        this.repository = repository;
        this.usuarioRepository = usuarioRepository;
        this.auditLogService = auditLogService;
        this.uploadsDir = Paths.get(uploadsDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.uploadsDir);
        } catch (IOException e) {
            throw new RuntimeException("Não foi possível criar o diretório de uploads: " + uploadsDir, e);
        }
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
    public List<ResultadoExameDTO> findByUsuario(Long usuarioId) {
        return repository.findByUsuarioId(usuarioId).stream().map(this::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public List<ResultadoExameDTO> findByUsuarioAutenticado(String username) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: " + username));
        return repository.findByUsuarioId(usuario.getId()).stream().map(this::toDTO).toList();
    }

    public ResultadoExameDTO create(ResultadoExameCreateDTO dto) {
        Usuario usuario = usuarioRepository.findById(dto.usuarioId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", dto.usuarioId()));

        ResultadoExame resultado = new ResultadoExame();
        resultado.setDescricao(dto.descricao());
        resultado.setDataEmissao(LocalDateTime.now());
        resultado.setLaudo(dto.laudo());
        resultado.setArquivoUrl(dto.arquivoUrl());
        resultado.setStatus(ResultadoExameStatus.PENDENTE);
        resultado.setUsuario(usuario);

        ResultadoExame saved = repository.save(resultado);
        auditLogService.log("CRIACAO_RESULTADO_EXAME", "ResultadoExame", saved.getId().toString(),
                "Resultado de exame criado para o usuário: " + usuario.getUsername());
        return toDTO(saved);
    }

    public ResultadoExameDTO update(Long id, ResultadoExameUpdateDTO dto) {
        ResultadoExame resultado = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resultado de Exame", id));

        if (dto.descricao() != null) resultado.setDescricao(dto.descricao());
        if (dto.laudo() != null) resultado.setLaudo(dto.laudo());
        if (dto.arquivoUrl() != null) resultado.setArquivoUrl(dto.arquivoUrl());
        if (dto.status() != null) resultado.setStatus(dto.status());

        ResultadoExame saved = repository.save(resultado);
        auditLogService.log("ALTERACAO_RESULTADO_EXAME", "ResultadoExame", saved.getId().toString(),
                "Resultado de exame #" + saved.getId() + " atualizado");
        return toDTO(saved);
    }

    public void delete(Long id) {
        ResultadoExame resultado = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resultado de Exame", id));
        String storedPath = resultado.getArquivoUrl();
        if (storedPath != null && !storedPath.startsWith("http")) {
            Path filePath = uploadsDir.resolve(storedPath).normalize();
            if (filePath.startsWith(uploadsDir)) {
                try { Files.deleteIfExists(filePath); } catch (IOException ignored) {}
            }
        }
        repository.deleteById(id);
        auditLogService.log("EXCLUSAO_RESULTADO_EXAME", "ResultadoExame", id.toString(),
                "Resultado de exame #" + id + " excluído");
    }

    public ResultadoExameDTO visualizar(Long id, String authenticatedUsername) {
        ResultadoExame resultado = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resultado de Exame", id));
        if (!resultado.getUsuario().getUsername().equals(authenticatedUsername)) {
            throw new BusinessException("Acesso negado.");
        }
        ResultadoExameStatus current = resultado.getStatus();
        if (current == ResultadoExameStatus.PENDENTE || current == ResultadoExameStatus.EM_ANDAMENTO) {
            resultado.setStatus(ResultadoExameStatus.VISUALIZADO);
            repository.save(resultado);
        }
        return toDTO(resultado);
    }

    public ResultadoExameDTO uploadArquivo(Long id, MultipartFile file) {
        ResultadoExame resultado = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resultado de Exame", id));

        if (file.isEmpty()) {
            throw new BusinessException("Arquivo não pode ser vazio.");
        }

        String originalName = Paths.get(file.getOriginalFilename()).getFileName().toString();
        String safeName = id + "-" + UUID.randomUUID() + "-" + originalName;
        Path target = uploadsDir.resolve(safeName);

        try {
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Falha ao salvar arquivo: " + e.getMessage(), e);
        }

        resultado.setArquivoUrl(safeName);
        return toDTO(repository.save(resultado));
    }

    public Resource downloadArquivo(Long id, String authenticatedUsername) {
        ResultadoExame resultado = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resultado de Exame", id));

        boolean isOwner = resultado.getUsuario().getUsername().equals(authenticatedUsername);
        boolean isAdmin = usuarioRepository.findByUsername(authenticatedUsername)
                .map(u -> "ADMIN".equals(u.getRole()))
                .orElse(false);

        if (!isOwner && !isAdmin) {
            throw new BusinessException("Acesso negado.");
        }

        String storedPath = resultado.getArquivoUrl();
        if (storedPath == null || storedPath.startsWith("http")) {
            throw new BusinessException("Este resultado não possui arquivo para download.");
        }

        Path filePath = uploadsDir.resolve(storedPath).normalize();
        if (!filePath.startsWith(uploadsDir)) {
            throw new BusinessException("Caminho de arquivo inválido.");
        }

        try {
            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new BusinessException("Arquivo não encontrado no servidor.");
            }
            return resource;
        } catch (MalformedURLException e) {
            throw new RuntimeException("Erro ao localizar arquivo.", e);
        }
    }

    private ResultadoExameDTO toDTO(ResultadoExame r) {
        return new ResultadoExameDTO(
                r.getId(),
                r.getDescricao(),
                r.getDataEmissao(),
                r.getLaudo(),
                r.getArquivoUrl(),
                r.getUsuario().getId(),
                r.getUsuario().getUsername(),
                r.getStatus()
        );
    }
}
