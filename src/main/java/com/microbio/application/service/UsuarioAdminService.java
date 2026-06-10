package com.microbio.application.service;

import com.microbio.application.dto.UsuarioCreateDTO;
import com.microbio.application.dto.UsuarioResponseDTO;
import com.microbio.application.dto.UsuarioUpdateDTO;
import com.microbio.application.exception.BusinessException;
import com.microbio.application.exception.ResourceNotFoundException;
import com.microbio.application.model.Pessoa;
import com.microbio.application.model.Usuario;
import com.microbio.application.repository.PessoaRepository;
import com.microbio.application.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class UsuarioAdminService {

    private final UsuarioRepository repository;
    private final PessoaRepository pessoaRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;

    public UsuarioAdminService(UsuarioRepository repository,
                               PessoaRepository pessoaRepository,
                               PasswordEncoder passwordEncoder,
                               AuditLogService auditLogService) {
        this.repository = repository;
        this.pessoaRepository = pessoaRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponseDTO> findAll() {
        return repository.findAll().stream().map(this::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponseDTO> findByRole(String role) {
        return repository.findByRole(role).stream().map(this::toDTO).toList();
    }

    private boolean isExecutorMaster() {
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal().toString())) {
            return false;
        }
        String currentUsername = auth.getName();
        return repository.findByUsername(currentUsername)
                .map(u -> "ADMIN_MASTER".equals(u.getRole()))
                .orElse(false);
    }

    public UsuarioResponseDTO create(UsuarioCreateDTO dto) {
        if (repository.findByUsername(dto.username()).isPresent()) {
            throw new BusinessException("Já existe um usuário com o username: " + dto.username());
        }

        String role = dto.role() != null && !dto.role().isBlank() ? dto.role() : "USER";
        if (role.startsWith("ROLE_")) {
            role = role.substring(5);
        }

        if (("ADMIN".equals(role) || "ADMIN_MASTER".equals(role)) && !isExecutorMaster()) {
            throw new BusinessException("Apenas administradores Master podem gerenciar contas administrativas.");
        }

        Usuario usuario = new Usuario();
        usuario.setUsername(dto.username());
        usuario.setSenha(passwordEncoder.encode(dto.password()));
        usuario.setRole(role);
        usuario.setPessoaId(dto.pessoaId());

        Usuario saved = repository.save(usuario);
        auditLogService.log("CRIACAO_USUARIO", "Usuario", saved.getId().toString(),
                "Usuário '" + saved.getUsername() + "' com perfil '" + saved.getRole() + "' criado");
        return toDTO(saved);
    }

    public UsuarioResponseDTO update(Long id, UsuarioUpdateDTO dto) {
        Usuario usuario = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", id));

        if (("ADMIN".equals(usuario.getRole()) || "ADMIN_MASTER".equals(usuario.getRole())) && !isExecutorMaster()) {
            throw new BusinessException("Apenas administradores Master podem atualizar contas administrativas.");
        }

        if (dto.username() != null && !dto.username().isBlank()) {
            repository.findByUsername(dto.username())
                    .filter(u -> !u.getId().equals(id))
                    .ifPresent(u -> { throw new BusinessException("Já existe um usuário com o username: " + dto.username()); });
            usuario.setUsername(dto.username());
        }

        if (dto.password() != null && !dto.password().isBlank()) {
            usuario.setSenha(passwordEncoder.encode(dto.password()));
        }

        Usuario saved = repository.save(usuario);
        auditLogService.log("ALTERACAO_USUARIO", "Usuario", saved.getId().toString(),
                "Usuário '" + saved.getUsername() + "' atualizado");
        return toDTO(saved);
    }

    public void delete(Long id) {
        Usuario usuario = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", id));

        if (("ADMIN".equals(usuario.getRole()) || "ADMIN_MASTER".equals(usuario.getRole())) && !isExecutorMaster()) {
            throw new BusinessException("Apenas administradores Master podem excluir contas administrativas.");
        }

        repository.deleteById(id);
        auditLogService.log("EXCLUSAO_USUARIO", "Usuario", id.toString(),
                "Usuário '" + usuario.getUsername() + "' excluído");
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponseDTO> findAdministradores() {
        return repository.findByRoleIn(List.of("ADMIN", "ADMIN_MASTER"))
                .stream()
                .map(this::toDTO)
                .toList();
    }

    private UsuarioResponseDTO toDTO(Usuario u) {
        String nomePessoa = null;
        if (u.getPessoaId() != null) {
            nomePessoa = pessoaRepository.findById(u.getPessoaId())
                    .map(Pessoa::getNome)
                    .orElse(null);
        }
        return new UsuarioResponseDTO(u.getId(), u.getUsername(), u.getRole(), u.getPessoaId(), nomePessoa);
    }
}
