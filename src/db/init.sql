-- =========================
-- TABELA PESSOA
-- =========================
CREATE TABLE pessoa (
                        pessoa_id SERIAL PRIMARY KEY,
                        primeiro_nome VARCHAR(100) NOT NULL,
                        sobrenome VARCHAR(100) NOT NULL,
                        data_nascimento DATE NOT NULL
                            CHECK (data_nascimento >= DATE '1900-01-01' AND data_nascimento <= CURRENT_DATE)
);

-- =========================
-- TABELA USUARIO
-- =========================
DROP TABLE IF EXISTS usuario CASCADE;

CREATE TABLE usuario (
                         id SERIAL PRIMARY KEY,
                         email VARCHAR(150) UNIQUE NOT NULL,
                         senha VARCHAR(255) NOT NULL,
                         role VARCHAR(50) NOT NULL
);

-- =========================
-- TABELA ENDERECO
-- =========================
CREATE TABLE endereco (
                          endereco_id SERIAL PRIMARY KEY,
                          estado_uf CHAR(2) NOT NULL
                              CHECK (estado_uf IN (
                                                   'AC','AL','AP','AM','BA','CE','DF','ES','GO','MA','MT','MS',
                                                   'MG','PA','PB','PR','PE','PI','RJ','RN','RS','RO','RR','SC',
                                                   'SP','SE','TO'
                                  )),
                          cep VARCHAR(10),
                          cidade VARCHAR(100),
                          rua VARCHAR(150),
                          bairro VARCHAR(100),
                          numero INTEGER,
                          pessoa_id INTEGER,
                          CONSTRAINT fk_endereco_pessoa
                              FOREIGN KEY (pessoa_id) REFERENCES pessoa(pessoa_id)
);

-- =========================
-- TABELA NOTICIAS
-- =========================
CREATE TABLE noticias (
                          noticia_id SERIAL PRIMARY KEY,
                          titulo VARCHAR(150) NOT NULL,
                          conteudo TEXT NOT NULL,
                          resumo TEXT,
                          data_publicacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          imagem_capa VARCHAR(255),
                          usuario_id INTEGER,
                          CONSTRAINT fk_noticia_usuario
                              FOREIGN KEY (usuario_id) REFERENCES usuario(usuario_id)
);

-- =========================
-- TABELA SERVICOS
-- =========================
CREATE TABLE servicos (
                          servico_id SERIAL PRIMARY KEY,
                          nome_servico VARCHAR(150) NOT NULL,
                          descricao TEXT
);

-- =========================
-- PERGUNTAS DOS SERVIÇOS
-- =========================
CREATE TABLE pergunta_servico (
                                  pergunta_id SERIAL PRIMARY KEY,
                                  descricao TEXT NOT NULL,
                                  servico_id INTEGER,
                                  CONSTRAINT fk_pergunta_servico
                                      FOREIGN KEY (servico_id) REFERENCES servicos(servico_id)
);

-- =========================
-- ORCAMENTO
-- =========================
CREATE TABLE orcamento (
                           orcamento_id SERIAL PRIMARY KEY,
                           pessoa_id INTEGER,
                           data_solicitacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                           status VARCHAR(20) DEFAULT 'pendente',
                           CONSTRAINT fk_orcamento_pessoa
                               FOREIGN KEY (pessoa_id) REFERENCES pessoa(pessoa_id)
);

-- =========================
-- RESPOSTAS DO ORCAMENTO
-- =========================
CREATE TABLE resposta_orcamento (
                                    resposta_id SERIAL PRIMARY KEY,
                                    resposta TEXT,
                                    orcamento_id INTEGER,
                                    pergunta_id INTEGER,
                                    CONSTRAINT fk_resposta_orcamento
                                        FOREIGN KEY (orcamento_id) REFERENCES orcamento(orcamento_id),
                                    CONSTRAINT fk_resposta_pergunta
                                        FOREIGN KEY (pergunta_id) REFERENCES pergunta_servico(pergunta_id)
);

-- =========================
-- RESULTADO EXAMES
-- =========================
CREATE TABLE resultado_exames (
                                  resultado_id SERIAL PRIMARY KEY,
                                  pessoa_id INTEGER,
                                  nome_exame VARCHAR(150),
                                  descricao TEXT,
                                  data_exame DATE,
                                  arquivo VARCHAR(255),
                                  status VARCHAR(20) DEFAULT 'ativo',
                                  CONSTRAINT fk_resultado_pessoa
                                      FOREIGN KEY (pessoa_id) REFERENCES pessoa(pessoa_id)
);