-- Block 2+3: Per-pitcher pitch count and current pitcher tracking

ALTER TABLE baseball_game_state
    ADD COLUMN IF NOT EXISTS current_pitcher_player_match_id BIGINT REFERENCES player_match(id);

CREATE TABLE IF NOT EXISTS pitcher_pitch_count (
    id                       BIGSERIAL PRIMARY KEY,
    game_state_id            BIGINT  NOT NULL REFERENCES baseball_game_state(id) ON DELETE CASCADE,
    pitcher_player_match_id  BIGINT  NOT NULL REFERENCES player_match(id),
    pitch_count              INTEGER NOT NULL DEFAULT 0,
    UNIQUE (game_state_id, pitcher_player_match_id)
);

CREATE INDEX IF NOT EXISTS idx_ppc_game_state ON pitcher_pitch_count(game_state_id);
