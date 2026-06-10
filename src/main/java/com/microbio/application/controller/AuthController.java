package com.microbio.application.controller;

import com.microbio.application.dto.AuthResponse;
import com.microbio.application.dto.LoginRequest;
import com.microbio.application.exception.BusinessException;
import com.microbio.application.exception.ResourceNotFoundException;
import com.microbio.application.model.Usuario;
import com.microbio.application.repository.PessoaRepository;
import com.microbio.application.repository.UsuarioRepository;
import com.microbio.application.security.JwtUtil;
import com.microbio.application.service.AuditLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticação", description = "Endpoints de autenticação")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UsuarioRepository usuarioRepository;
    private final PessoaRepository pessoaRepository;
    private final AuditLogService auditLogService;

    public AuthController(AuthenticationManager authenticationManager,
                          JwtUtil jwtUtil,
                          UsuarioRepository usuarioRepository,
                          PessoaRepository pessoaRepository,
                          AuditLogService auditLogService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.usuarioRepository = usuarioRepository;
        this.pessoaRepository = pessoaRepository;
        this.auditLogService = auditLogService;
    }

    @PostMapping("/login")
    @Operation(summary = "Realizar login e obter token JWT")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.username(),
                            loginRequest.password()
                    )
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String token = jwtUtil.generateToken(userDetails);
            String role = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .findFirst()
                    .orElse("ROLE_USER");

            String roleName = role.startsWith("ROLE_") ? role.substring(5) : role;
            auditLogService.log(userDetails.getUsername(), roleName, "LOGIN", "Usuario", userDetails.getUsername(), "Login realizado com sucesso");

            return ResponseEntity.ok(new AuthResponse(true, "Login realizado com sucesso", userDetails.getUsername(), token, role));

        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthResponse(false, "Usuário ou senha incorretos", null, null, null));
        }
    }

    @GetMapping("/me")
    @Operation(summary = "Retorna informações do usuário autenticado")
    public ResponseEntity<AuthResponse> me(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthResponse(false, "Não autenticado", null, null, null));
        }
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String role = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst().orElse("");
        return ResponseEntity.ok(new AuthResponse(true, "Autenticado", userDetails.getUsername(), null, role));
    }

    @PutMapping("/vincular-pessoa/{pessoaId}")
    @Transactional
    @Operation(summary = "Vincula o usuário autenticado a uma pessoa cadastrada")
    public ResponseEntity<AuthResponse> vincularPessoa(
            @PathVariable Long pessoaId,
            Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthResponse(false, "Não autenticado", null, null, null));
        }

        if (!pessoaRepository.existsById(pessoaId)) {
            throw new ResourceNotFoundException("Pessoa", pessoaId);
        }

        String username = authentication.getName();
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("Usuário não encontrado: " + username));

        usuario.setPessoaId(pessoaId);
        usuarioRepository.save(usuario);

        auditLogService.log(username, usuario.getRole(), "VINCULAR_PESSOA", "Usuario", username, "Usuário vinculado à Pessoa ID: " + pessoaId);

        return ResponseEntity.ok(new AuthResponse(true, "Pessoa vinculada com sucesso", username, null, usuario.getRole()));
    }

    @PostMapping("/logout")
    @Operation(summary = "Registrar evento de logout")
    public ResponseEntity<Void> logout(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            String role = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .findFirst()
                    .orElse("USER");
            String roleName = role.startsWith("ROLE_") ? role.substring(5) : role;
            auditLogService.log(username, roleName, "LOGOUT", "Usuario", username, "Logout realizado com sucesso");
        }
        return ResponseEntity.ok().build();
    }
}
