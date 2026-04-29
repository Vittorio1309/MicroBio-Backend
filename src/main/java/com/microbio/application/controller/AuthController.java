package com.microbio.application.controller;

import com.microbio.application.dto.LoginRequest;
import com.microbio.application.dto.AuthResponse;
import com.microbio.application.model.Usuario;
import com.microbio.application.repository.UsuarioRepository;
import com.microbio.application.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"}, methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.OPTIONS})
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;

    public AuthController(AuthenticationManager authenticationManager, JwtService jwtService, UsuarioRepository usuarioRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Login via email/username e senha
     * Retorna JWT token para uso em requisições autenticadas
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest loginRequest) {
        try {
            // Autenticar usuário
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.username(),
                            loginRequest.password()
                    )
            );

            // Recuperar usuário para obter role
            Usuario usuario = usuarioRepository.findByUsername(loginRequest.username())
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

            // Gerar JWT token
            String token = jwtService.generateToken(authentication);

            // Retornar resposta com token
            AuthResponse response = new AuthResponse(
                    true,
                    "Login realizado com sucesso",
                    usuario.getUsername(),
                    token,
                    usuario.getRole()
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthResponse(
                            false,
                            "Usuário ou senha incorretos",
                            null
                    ));
        }
    }

    /**
     * Logout - limpa o contexto de segurança
     */
    @PostMapping("/logout")
    public ResponseEntity<AuthResponse> logout() {
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(new AuthResponse(
                true,
                "Logout realizado com sucesso",
                null
        ));
    }

    /**
     * Obtém informações do usuário autenticado
     */
    @GetMapping("/me")
    public ResponseEntity<AuthResponse> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated() && !authentication.getName().equals("anonymousUser")) {
            Usuario usuario = usuarioRepository.findByUsername(authentication.getName())
                    .orElse(null);

            if (usuario != null) {
                return ResponseEntity.ok(new AuthResponse(
                        true,
                        "Usuário autenticado",
                        usuario.getUsername(),
                        null,
                        usuario.getRole()
                ));
            }
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new AuthResponse(
                        false,
                        "Não autenticado",
                        null
                ));
    }
}

