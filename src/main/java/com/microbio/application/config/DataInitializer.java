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
            // Remove legacy columns and constraints left over from schema refactorings.
            // ddl-auto=update adds new columns/constraints but never drops old ones.
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
            // preco was previously NOT NULL; it is now optional (nullable) in the entity.
            try {
                jdbcTemplate.execute("ALTER TABLE servico ALTER COLUMN preco DROP NOT NULL");
            } catch (Exception e) {
                System.out.println("Note: servico preco column check skipped or not supported by current database dialect.");
            }

            // Pessoas
            List<Pessoa> pessoas;
            if (pessoaRepository.count() == 0) {
                pessoas = pessoaRepository.saveAll(List.of(
                    new Pessoa("João Silva",    "joao@microbio.com.br",     "(41) 99999-0001"),
                    new Pessoa("Ana Souza",     "ana@microbio.com.br",      "(41) 99999-0002"),
                    new Pessoa("Maria Eduardo", "maria@microbio.com.br",    "(41) 99999-0003"),
                    new Pessoa("Carlos Lima",   "carlos@microbio.com.br",   "(41) 99999-0004"),
                    new Pessoa("Fernanda Melo", "fernanda@microbio.com.br", "(41) 99999-0005")
                ));
                System.out.println("✓ Pessoas de exemplo criadas");
            } else {
                pessoas = pessoaRepository.findAll();
            }

            // Usuários
            if (usuarioRepository.count() == 0) {
                Usuario admin = new Usuario("admin", passwordEncoder.encode("admin123"), "ADMIN");
                Usuario user  = new Usuario("user",  passwordEncoder.encode("user123"),  "USER");
                user.setPessoaId(pessoas.get(0).getId());
                usuarioRepository.save(admin);
                usuarioRepository.save(user);
                System.out.println("✓ Usuários iniciais criados: admin/admin123 | user/user123");
            }

            // Criar Pessoa Master para o perfil
            Pessoa pessoaMaster = pessoaRepository.findAll().stream()
                    .filter(p -> "Master".equals(p.getNome()))
                    .findFirst()
                    .orElseGet(() -> pessoaRepository.save(new Pessoa("Master", "master@microbio.com.br", "(41) 99999-9999")));

            java.util.Optional<Usuario> masterOpt = usuarioRepository.findByUsername("master");
            if (masterOpt.isEmpty()) {
                Usuario master = new Usuario("master", passwordEncoder.encode("Master123"), "ADMIN_MASTER");
                master.setPessoaId(pessoaMaster.getId());
                usuarioRepository.save(master);
                System.out.println("✓ Usuário master de demonstração criado: master/Master123");
            } else {
                Usuario master = masterOpt.get();
                master.setSenha(passwordEncoder.encode("Master123"));
                master.setRole("ADMIN_MASTER");
                master.setPessoaId(pessoaMaster.getId());
                usuarioRepository.save(master);
                System.out.println("✓ Usuário master de demonstração atualizado com a senha Master123");
            }

            // Usuário MicroBio master principal
            if (usuarioRepository.findByUsername("MicroBio").isEmpty()) {
                Usuario microBio = new Usuario("MicroBio", passwordEncoder.encode("1234"), "ADMIN_MASTER");
                usuarioRepository.save(microBio);
                System.out.println("✓ Usuário principal criado: MicroBio/1234");
            }

            // Configuração padrão de prazo de acompanhamento
            if (configuracaoRepository.findByChave("prazo_acompanhamento_orcamentos").isEmpty()) {
                configuracaoRepository.save(new Configuracao("prazo_acompanhamento_orcamentos", "48 horas"));
                System.out.println("✓ Configuração padrão criada: prazo_acompanhamento_orcamentos=48 horas");
            }

            // Serviços
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

            // Orçamentos de exemplo
            if (orcamentoRepository.count() == 0) {
                List<Servico> servicos = servicoRepository.findAll();
                if (!servicos.isEmpty()) {
                    Servico agua     = servicos.get(0);
                    Servico solo     = servicos.size() > 1 ? servicos.get(1) : servicos.get(0);
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
