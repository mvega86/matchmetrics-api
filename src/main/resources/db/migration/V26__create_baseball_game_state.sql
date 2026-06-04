-- Migración: Crear tabla baseball_game_state para gestión de estado en vivo de partidos de béisbol/softbol

-- Crear tabla baseball_game_state
CREATE TABLE baseball_game_state (
    id BIGSERIAL PRIMARY KEY,
    match_id BIGINT NOT NULL UNIQUE,
    current_inning INTEGER NOT NULL DEFAULT 1,
    inning_half VARCHAR(20) NOT NULL DEFAULT 'TOP',
    outs INTEGER NOT NULL DEFAULT 0,
    balls INTEGER NOT NULL DEFAULT 0,
    strikes INTEGER NOT NULL DEFAULT 0,
    home_score INTEGER NOT NULL DEFAULT 0,
    away_score INTEGER NOT NULL DEFAULT 0,
    first_base_player_match_id BIGINT,
    second_base_player_match_id BIGINT,
    third_base_player_match_id BIGINT,
    status VARCHAR(50) NOT NULL DEFAULT 'NOT_STARTED',
    created_at TIMESTAMP DEFAULT now(),
    updated_at TIMESTAMP DEFAULT now(),
    modified_by VARCHAR(255),
    
    -- Constraint para asegurar valores válidos
    CONSTRAINT chk_baseball_state_inning_half CHECK (inning_half IN ('TOP', 'BOTTOM')),
    CONSTRAINT chk_baseball_state_status CHECK (status IN ('NOT_STARTED', 'IN_PROGRESS', 'SUSPENDED', 'FINISHED')),
    CONSTRAINT chk_baseball_state_outs CHECK (outs >= 0 AND outs <= 3),
    CONSTRAINT chk_baseball_state_balls CHECK (balls >= 0 AND balls <= 3),
    CONSTRAINT chk_baseball_state_strikes CHECK (strikes >= 0 AND strikes <= 2),
    
    -- Foreign keys
    CONSTRAINT fk_baseball_game_state_match FOREIGN KEY (match_id) REFERENCES match(id) ON DELETE CASCADE,
    CONSTRAINT fk_baseball_game_state_first_base FOREIGN KEY (first_base_player_match_id) REFERENCES player_match(id) ON DELETE SET NULL,
    CONSTRAINT fk_baseball_game_state_second_base FOREIGN KEY (second_base_player_match_id) REFERENCES player_match(id) ON DELETE SET NULL,
    CONSTRAINT fk_baseball_game_state_third_base FOREIGN KEY (third_base_player_match_id) REFERENCES player_match(id) ON DELETE SET NULL
);

-- Crear índices
CREATE INDEX idx_baseball_game_state_match_id ON baseball_game_state(match_id);

-- Trigger para actualizar updated_at
CREATE TRIGGER trigger_set_updated_at_baseball_game_state
BEFORE UPDATE ON baseball_game_state
FOR EACH ROW
EXECUTE FUNCTION set_updated_at();
