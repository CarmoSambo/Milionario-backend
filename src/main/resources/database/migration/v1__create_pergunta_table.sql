-- Criar tabelas para o jogo Milionário

-- Tabela para sessões de jogo
CREATE TABLE IF NOT EXISTS game_session (
    id BIGSERIAL PRIMARY KEY,
    current_question_index INTEGER NOT NULL DEFAULT 0,
    score INTEGER NOT NULL DEFAULT 0,
    finished BOOLEAN NOT NULL DEFAULT FALSE,
    current_level VARCHAR(20) NOT NULL DEFAULT 'EASY',
    status VARCHAR(20) NOT NULL DEFAULT 'IN_PROCESS',
    used_fifty_fifty BOOLEAN NOT NULL DEFAULT FALSE,
    used_skip BOOLEAN NOT NULL DEFAULT FALSE,
    question_start_time TIMESTAMP,
    used_ask_audience BOOLEAN NOT NULL DEFAULT FALSE,
    used_phone_friend BOOLEAN NOT NULL DEFAULT FALSE,
    current_question_id BIGINT,
    question_ids VARCHAR(1000)
);

-- Tabela para perguntas
-- CORRIGIDO: removidas colunas option_a/b/c/d e correct_answer que nunca foram usadas
CREATE TABLE IF NOT EXISTS question (
    id BIGSERIAL PRIMARY KEY,
    question TEXT NOT NULL,
    difficulty VARCHAR(20) NOT NULL
);

-- Tabela para respostas
CREATE TABLE IF NOT EXISTS answer (
    id BIGSERIAL PRIMARY KEY,
    text VARCHAR(500) NOT NULL,
    correct BOOLEAN NOT NULL DEFAULT FALSE,
    question_id BIGINT NOT NULL,
    FOREIGN KEY (question_id) REFERENCES question(id) ON DELETE CASCADE
);

-- Tabela para pontuações
CREATE TABLE IF NOT EXISTS score (
    id BIGSERIAL PRIMARY KEY,
    nickname VARCHAR(100) NOT NULL,
    points INTEGER NOT NULL DEFAULT 0
);

-- Índices para melhor performance
CREATE INDEX IF NOT EXISTS idx_question_difficulty ON question(difficulty);
CREATE INDEX IF NOT EXISTS idx_answer_question_id ON answer(question_id);
CREATE INDEX IF NOT EXISTS idx_score_points ON score(points DESC);
CREATE INDEX IF NOT EXISTS idx_game_session_status ON game_session(status);
