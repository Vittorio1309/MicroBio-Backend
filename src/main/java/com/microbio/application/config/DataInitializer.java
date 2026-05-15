package com.microbio.application.config;

import com.microbio.application.model.*;
import com.microbio.application.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initializeData(
            UsuarioRepository usuarioRepository,
            ServicoRepository servicoRepository,
            PerguntaServicoRepository perguntaRepository,
            PessoaRepository pessoaRepository,
            PasswordEncoder passwordEncoder) {
        return args -> {
            // Pessoa de exemplo
            Pessoa pessoaExemplo = null;
            if (pessoaRepository.count() == 0) {
                pessoaExemplo = pessoaRepository.save(new Pessoa("Cliente Exemplo", "cliente@microbio.com.br", passwordEncoder.encode("cliente123"), "(41) 99999-0000"));
                System.out.println("✓ Pessoa de exemplo criada");
            } else {
                pessoaExemplo = pessoaRepository.findAll().get(0);
            }

            // Usuários
            if (usuarioRepository.count() == 0) {
                Usuario admin = new Usuario("admin", passwordEncoder.encode("admin123"), "ADMIN");
                Usuario user = new Usuario("user", passwordEncoder.encode("user123"), "USER");
                if (pessoaExemplo != null) {
                    user.setPessoaId(pessoaExemplo.getId());
                }
                usuarioRepository.save(admin);
                usuarioRepository.save(user);
                System.out.println("✓ Usuários iniciais criados: admin/admin123 | user/user123");
            }

            // Serviços e perguntas de exemplo
            if (servicoRepository.count() == 0) {
                Servico s1 = new Servico();
                s1.setNome("Análise Microbiológica de Água");
                s1.setDescricao("Análise completa de coliformes totais, E. coli e bactérias heterotróficas");
                s1.setPreco(new BigDecimal("350.00"));
                servicoRepository.save(s1);

                perguntaRepository.save(criarPergunta("Qual a origem da água? (poço, torneira, rio, etc.)", true, s1));
                perguntaRepository.save(criarPergunta("Há algum tratamento prévio da água?", true, s1));
                perguntaRepository.save(criarPergunta("Qual a finalidade do uso da água?", false, s1));

                Servico s2 = new Servico();
                s2.setNome("Análise de Solo Agrícola");
                s2.setDescricao("Análise química e microbiológica de solo para fins agronômicos");
                s2.setPreco(new BigDecimal("280.00"));
                servicoRepository.save(s2);

                perguntaRepository.save(criarPergunta("Qual cultura será plantada no solo?", true, s2));
                perguntaRepository.save(criarPergunta("O solo já recebeu alguma adubação recente?", true, s2));
                perguntaRepository.save(criarPergunta("Qual a área total em hectares?", false, s2));

                Servico s3 = new Servico();
                s3.setNome("Análise de Alimentos");
                s3.setDescricao("Análise microbiológica de alimentos para segurança alimentar");
                s3.setPreco(new BigDecimal("420.00"));
                servicoRepository.save(s3);

                perguntaRepository.save(criarPergunta("Qual o tipo de alimento a ser analisado?", true, s3));
                perguntaRepository.save(criarPergunta("Qual a data de produção/validade?", true, s3));

                System.out.println("✓ Serviços e perguntas de exemplo criados");
            }
        };
    }

    private PerguntaServico criarPergunta(String texto, boolean obrigatoria, Servico servico) {
        PerguntaServico p = new PerguntaServico();
        p.setPergunta(texto);
        p.setObrigatoria(obrigatoria);
        p.setServico(servico);
        return p;
    }
}
