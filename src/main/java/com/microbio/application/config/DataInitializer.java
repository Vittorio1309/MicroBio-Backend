package com.microbio.application.config;

import com.microbio.application.model.*;
import com.microbio.application.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Configuration
public class DataInitializer {

    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_ADMIN_MASTER = "ADMIN_MASTER";
    private static final String USERNAME_MICROBIO = "MicroBio";
    private static final String SEED_PASSWORD_ADMIN = "admin123";
    private static final String SEED_PASSWORD_USER = "user123";
    private static final String SEED_PASSWORD_MICROBIO = "1234";

    @Bean
    public CommandLineRunner initializeData(
            UsuarioRepository usuarioRepository,
            ServicoRepository servicoRepository,
            PerguntaServicoRepository perguntaRepository,
            PessoaRepository pessoaRepository,
            OrcamentoRepository orcamentoRepository,
            ConfiguracaoRepository configuracaoRepository,
            PasswordEncoder passwordEncoder,
            JdbcTemplate jdbcTemplate) {
        return args -> {
            executarMigracoesDDL(jdbcTemplate);
            List<Pessoa> pessoas = inicializarPessoas(pessoaRepository);
            criarUsuariosIniciais(usuarioRepository, pessoas, passwordEncoder);
            criarUsuarioMicroBio(usuarioRepository, passwordEncoder);
            migrarRolesLegados(usuarioRepository);
            inicializarConfiguracoes(configuracaoRepository);
            inicializarServicos(servicoRepository, perguntaRepository);
            inicializarOrcamentos(orcamentoRepository, servicoRepository, pessoas);
        };
    }

    private void executarMigracoesDDL(JdbcTemplate jdbcTemplate) {
        try {
            jdbcTemplate.execute("ALTER TABLE resultado_exame DROP COLUMN IF EXISTS orcamento_id CASCADE");
        } catch (Exception e) {
            System.out.println("Note: orcamento_id legacy column check skipped or not supported by current database dialect.");
        }
        try {
            jdbcTemplate.execute("ALTER TABLE resultado_exame DROP COLUMN IF EXISTS pessoa_id CASCADE");
        } catch (Exception e) {
            System.out.println("Note: pessoa_id legacy column check skipped or not supported by current database dialect.");
        }
        try {
            jdbcTemplate.execute("ALTER TABLE servico ALTER COLUMN preco DROP NOT NULL");
        } catch (Exception e) {
            System.out.println("Note: servico preco column check skipped or not supported by current database dialect.");
        }
    }

    private List<Pessoa> inicializarPessoas(PessoaRepository pessoaRepository) {
        if (pessoaRepository.count() == 0) {
            List<Pessoa> criadas = pessoaRepository.saveAll(List.of(
                new Pessoa("João Silva",    "joao@microbio.com.br",     "(41) 99999-0001"),
                new Pessoa("Ana Souza",     "ana@microbio.com.br",      "(41) 99999-0002"),
                new Pessoa("Maria Eduardo", "maria@microbio.com.br",    "(41) 99999-0003"),
                new Pessoa("Carlos Lima",   "carlos@microbio.com.br",   "(41) 99999-0004"),
                new Pessoa("Fernanda Melo", "fernanda@microbio.com.br", "(41) 99999-0005")
            ));
            System.out.println("✓ Pessoas de exemplo criadas");
            return criadas;
        }
        return pessoaRepository.findAll();
    }

    private void criarUsuariosIniciais(UsuarioRepository usuarioRepository, List<Pessoa> pessoas, PasswordEncoder passwordEncoder) {
        if (usuarioRepository.count() == 0) {
            Usuario admin = new Usuario("admin", passwordEncoder.encode(SEED_PASSWORD_ADMIN), ROLE_ADMIN);
            Usuario user  = new Usuario("user",  passwordEncoder.encode(SEED_PASSWORD_USER),  "USER");
            user.setPessoaId(pessoas.get(0).getId());
            usuarioRepository.save(admin);
            usuarioRepository.save(user);
            System.out.println("✓ Usuários iniciais criados");
        }
    }

    private void criarUsuarioMicroBio(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        if (usuarioRepository.findByUsername(USERNAME_MICROBIO).isEmpty()) {
            Usuario microBio = new Usuario(USERNAME_MICROBIO, passwordEncoder.encode(SEED_PASSWORD_MICROBIO), ROLE_ADMIN);
            usuarioRepository.save(microBio);
            System.out.println("✓ Usuário principal criado: " + USERNAME_MICROBIO);
        } else {
            usuarioRepository.findByUsername(USERNAME_MICROBIO).ifPresent(u -> {
                if (ROLE_ADMIN_MASTER.equals(u.getRole())) {
                    u.setRole(ROLE_ADMIN);
                    usuarioRepository.save(u);
                    System.out.println("✓ Usuário " + USERNAME_MICROBIO + " migrado de " + ROLE_ADMIN_MASTER + " para " + ROLE_ADMIN);
                }
            });
        }
    }

    private void migrarRolesLegados(UsuarioRepository usuarioRepository) {
        usuarioRepository.findAll().stream()
                .filter(u -> ROLE_ADMIN_MASTER.equals(u.getRole()))
                .forEach(u -> {
                    u.setRole(ROLE_ADMIN);
                    usuarioRepository.save(u);
                    System.out.println("✓ Usuário '" + u.getUsername() + "' migrado de " + ROLE_ADMIN_MASTER + " para " + ROLE_ADMIN);
                });
    }

    private void inicializarConfiguracoes(ConfiguracaoRepository configuracaoRepository) {
        if (configuracaoRepository.findByChave("prazo_acompanhamento_orcamentos").isEmpty()) {
            configuracaoRepository.save(new Configuracao("prazo_acompanhamento_orcamentos", "48 horas"));
            System.out.println("✓ Configuração padrão criada: prazo_acompanhamento_orcamentos=48 horas");
        }
    }

    private void inicializarServicos(ServicoRepository servicoRepository, PerguntaServicoRepository perguntaRepository) {
        if (servicoRepository.count() != 0) {
            return;
        }
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

    private void inicializarOrcamentos(OrcamentoRepository orcamentoRepository, ServicoRepository servicoRepository, List<Pessoa> pessoas) {
        if (orcamentoRepository.count() != 0) {
            return;
        }
        List<Servico> servicos = servicoRepository.findAll();
        if (servicos.isEmpty()) {
            return;
        }
        Servico agua      = servicos.get(0);
        Servico solo      = servicos.size() > 1 ? servicos.get(1) : servicos.get(0);
        Servico alimentos = servicos.size() > 2 ? servicos.get(2) : servicos.get(0);

        Pessoa p0 = pessoas.get(0);
        Pessoa p1 = pessoas.size() > 1 ? pessoas.get(1) : p0;
        Pessoa p2 = pessoas.size() > 2 ? pessoas.get(2) : p0;
        Pessoa p3 = pessoas.size() > 3 ? pessoas.get(3) : p0;
        Pessoa p4 = pessoas.size() > 4 ? pessoas.get(4) : p0;

        orcamentoRepository.saveAll(List.of(
            criarOrcamento(p0, agua,      OrcamentoStatus.PENDENTE,   LocalDateTime.of(2025,  4, 21, 10, 0)),
            criarOrcamento(p1, solo,      OrcamentoStatus.FINALIZADO, LocalDateTime.of(2024,  1, 23, 14, 0)),
            criarOrcamento(p2, alimentos, OrcamentoStatus.PENDENTE,   LocalDateTime.of(2026,  3, 28,  9, 0)),
            criarOrcamento(p3, agua,      OrcamentoStatus.FINALIZADO, LocalDateTime.of(2026,  2, 10, 11, 0)),
            criarOrcamento(p4, solo,      OrcamentoStatus.PENDENTE,   LocalDateTime.of(2025,  5,  5, 15, 0))
        ));
        System.out.println("✓ Orçamentos de exemplo criados");
    }

    private PerguntaServico criarPergunta(String texto, boolean obrigatoria, Servico servico) {
        PerguntaServico p = new PerguntaServico();
        p.setPergunta(texto);
        p.setObrigatoria(obrigatoria);
        p.setServico(servico);
        return p;
    }

    private Orcamento criarOrcamento(Pessoa pessoa, Servico servico, OrcamentoStatus status, LocalDateTime data) {
        Orcamento o = new Orcamento();
        o.setDataCriacao(data);
        o.setStatus(status);
        o.setPessoa(pessoa);
        o.setServico(servico);
        o.setValorTotal(servico.getPreco());
        return o;
    }
}
