package com.microbio.application.config;

import com.microbio.application.model.Usuario;
import com.microbio.application.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initializeUsers(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            // Verificar se já existem usuários
            if (usuarioRepository.count() == 0) {
                // Criar usuário admin
                Usuario admin = new Usuario();
                admin.setUsername("admin");
                admin.setSenha(passwordEncoder.encode("admin123"));
                admin.setRole("ADMIN");
                usuarioRepository.save(admin);

                // Criar usuário comum
                Usuario user = new Usuario();
                user.setUsername("user");
                user.setSenha(passwordEncoder.encode("user123"));
                user.setRole("USER");
                usuarioRepository.save(user);

                System.out.println("✓ Usuários iniciais criados com sucesso!");
                System.out.println("  - admin / admin123 (ADMIN)");
                System.out.println("  - user / user123 (USER)");
            } else {
                System.out.println("✓ Usuários já existem no banco de dados.");
            }
        };
    }
}

