-- =========================
-- TABELA PESSOA
-- Corresponde a: model/Pessoa.java
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
-- Corresponde a: model/RespostaOrcamento.java
-- Os dados do formulário (perguntas + respostas) são salvos aqui.
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
-- Corresponde a: model/ResultadoExame.java
-- =========================
CREATE TABLE IF NOT EXISTS resultado_exame (
    id              BIGSERIAL PRIMARY KEY,
    data_exame      DATE,
    arquivo         VARCHAR(255),
    descricao       TEXT,
    status          VARCHAR(50),
    pessoa_id       BIGINT,
    orcamento_id    BIGINT,
    CONSTRAINT fk_resultado_pessoa   FOREIGN KEY (pessoa_id)   REFERENCES pessoa(id),
    CONSTRAINT fk_resultado_orcamento FOREIGN KEY (orcamento_id) REFERENCES orcamento(id)
);

-- =============================================================
-- DADOS INICIAIS (equivalente ao DataInitializer.java)
-- Executar apenas se o banco estiver vazio.
-- Na prática, o Spring Boot DataInitializer já faz isso via JPA.
-- =============================================================

-- Usuários de exemplo:
-- admin / admin123 (ROLE_ADMIN)
-- user  / user123  (ROLE_USER)

-- Serviços de exemplo com perguntas (inseridos pelo DataInitializer.java):
-- 1. Análise Microbiológica de Água   - R$ 350,00
-- 2. Análise de Solo Agrícola         - R$ 280,00
-- 3. Análise de Alimentos             - R$ 420,00
