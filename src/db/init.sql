-- =========================
-- TABELA PESSOA
-- Corresponde a: model/Pessoa.java
-- Sem coluna senha — autenticação é feita via tabela usuario
-- =========================
CREATE TABLE IF NOT EXISTS pessoa (
    id          BIGSERIAL PRIMARY KEY,
    nome        VARCHAR(255) NOT NULL,
    email       VARCHAR(255) NOT NULL UNIQUE,
    telefone    VARCHAR(50)
);

-- =========================
-- TABELA USUARIO
-- Corresponde a: model/Usuario.java
-- role armazenado SEM prefixo "ROLE_" (CustomUserDetailsService adiciona)
-- =========================
CREATE TABLE IF NOT EXISTS usuario (
    id          BIGSERIAL PRIMARY KEY,
    username    VARCHAR(255) NOT NULL UNIQUE,
    senha       VARCHAR(255) NOT NULL,
    role        VARCHAR(50)  NOT NULL,
    pessoa_id   BIGINT,
    CONSTRAINT fk_usuario_pessoa FOREIGN KEY (pessoa_id) REFERENCES pessoa(id)
);

-- =========================
-- TABELA SERVICO
-- Corresponde a: model/Servico.java
-- =========================
CREATE TABLE IF NOT EXISTS servico (
    id          BIGSERIAL PRIMARY KEY,
    nome        VARCHAR(255) NOT NULL,
    descricao   TEXT,
    preco       NUMERIC(19, 2)
);

-- =========================
-- TABELA PERGUNTA_SERVICO
-- Corresponde a: model/PerguntaServico.java
-- =========================
CREATE TABLE IF NOT EXISTS pergunta_servico (
    id          BIGSERIAL PRIMARY KEY,
    pergunta    TEXT         NOT NULL,
    obrigatoria BOOLEAN      NOT NULL DEFAULT false,
    servico_id  BIGINT,
    CONSTRAINT fk_pergunta_servico FOREIGN KEY (servico_id) REFERENCES servico(id)
);

-- =========================
-- TABELA ORCAMENTO
-- Corresponde a: model/Orcamento.java
-- =========================
CREATE TABLE IF NOT EXISTS orcamento (
    id              BIGSERIAL PRIMARY KEY,
    data_criacao    TIMESTAMP    NOT NULL,
    status          VARCHAR(50)  NOT NULL,
    observacao      TEXT,
    valor_total     NUMERIC(19, 2),
    pessoa_id       BIGINT       NOT NULL,
    servico_id      BIGINT       NOT NULL,
    CONSTRAINT fk_orcamento_pessoa  FOREIGN KEY (pessoa_id)  REFERENCES pessoa(id),
    CONSTRAINT fk_orcamento_servico FOREIGN KEY (servico_id) REFERENCES servico(id)
);

-- =========================
-- TABELA RESPOSTA_ORCAMENTO
-- =========================
CREATE TABLE IF NOT EXISTS resposta_orcamento (
    id              BIGSERIAL PRIMARY KEY,
    resposta        TEXT,
    pergunta_id     BIGINT,
    orcamento_id    BIGINT,
    CONSTRAINT fk_resposta_pergunta  FOREIGN KEY (pergunta_id)  REFERENCES pergunta_servico(id),
    CONSTRAINT fk_resposta_orcamento FOREIGN KEY (orcamento_id) REFERENCES orcamento(id)
);

-- =========================
-- TABELA RESULTADO_EXAME
-- =========================
CREATE TABLE IF NOT EXISTS resultado_exame (
    id              BIGSERIAL PRIMARY KEY,
    data_exame      DATE,
    arquivo         VARCHAR(255),
    descricao       TEXT,
    status          VARCHAR(50),
    pessoa_id       BIGINT,
    orcamento_id    BIGINT,
    CONSTRAINT fk_resultado_pessoa    FOREIGN KEY (pessoa_id)   REFERENCES pessoa(id),
    CONSTRAINT fk_resultado_orcamento FOREIGN KEY (orcamento_id) REFERENCES orcamento(id)
);

-- =============================================================
-- DADOS INICIAIS
-- O DataInitializer.java insere esses registros automaticamente
-- na primeira inicialização se as tabelas estiverem vazias.
--
-- Usuários pré-definidos (senhas codificadas via BCrypt):
--   admin / admin123  →  role = ADMIN
--   user  / user123   →  role = USER
--
-- Exemplo de inserção manual (BCrypt de "admin123"):
-- INSERT INTO usuario (username, senha, role)
--   VALUES ('admin', '$2a$10$...hash...', 'ADMIN')
--   ON CONFLICT (username) DO NOTHING;
-- =============================================================
