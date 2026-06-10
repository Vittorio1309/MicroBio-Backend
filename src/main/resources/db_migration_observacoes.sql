-- Migration: Criação da tabela observacao_orcamento

CREATE TABLE IF NOT EXISTS observacao_orcamento (
    id BIGSERIAL PRIMARY KEY,
    texto TEXT NOT NULL,
    data_criacao TIMESTAMP NOT NULL,
    orcamento_id BIGINT NOT NULL,
    usuario_id BIGINT NOT NULL,
    CONSTRAINT fk_observacao_orcamento_orcamento FOREIGN KEY (orcamento_id) REFERENCES orcamento (id) ON DELETE CASCADE,
    CONSTRAINT fk_observacao_orcamento_usuario FOREIGN KEY (usuario_id) REFERENCES usuario (id) ON DELETE RESTRICT
);

CREATE INDEX IF NOT EXISTS idx_observacao_orcamento_orc ON observacao_orcamento(orcamento_id);
