-- Crear tabla baseball_play_event para eventos narrados de béisbol/softbol
CREATE TABLE baseball_play_event (
    id BIGSERIAL PRIMARY KEY,
    match_id BIGINT NOT NULL,
    inning INTEGER NOT NULL,
    inning_half VARCHAR(20) NOT NULL,
    batting_team_id BIGINT NOT NULL,
    fielding_team_id BIGINT NOT NULL,
    batter_player_match_id BIGINT,
    pitcher_player_match_id BIGINT,
    event_type VARCHAR(50) NOT NULL,
    result VARCHAR(255),
    runs_scored INTEGER DEFAULT 0,
    outs_on_play INTEGER DEFAULT 0,
    rbi INTEGER DEFAULT 0,
    description VARCHAR(500),
    balls_before INTEGER DEFAULT 0,
    strikes_before INTEGER DEFAULT 0,
    outs_before INTEGER DEFAULT 0,
    first_base_before BOOLEAN DEFAULT FALSE,
    second_base_before BOOLEAN DEFAULT FALSE,
    third_base_before BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT now(),
    updated_at TIMESTAMP DEFAULT now(),
    modified_by VARCHAR(255),

    CONSTRAINT fk_baseball_play_event_match FOREIGN KEY (match_id) REFERENCES match(id) ON DELETE CASCADE,
    CONSTRAINT fk_baseball_play_event_batting_team FOREIGN KEY (batting_team_id) REFERENCES teams(id) ON DELETE SET NULL,
    CONSTRAINT fk_baseball_play_event_fielding_team FOREIGN KEY (fielding_team_id) REFERENCES teams(id) ON DELETE SET NULL,
    CONSTRAINT fk_baseball_play_event_batter_player_match FOREIGN KEY (batter_player_match_id) REFERENCES player_match(id) ON DELETE SET NULL,
    CONSTRAINT fk_baseball_play_event_pitcher_player_match FOREIGN KEY (pitcher_player_match_id) REFERENCES player_match(id) ON DELETE SET NULL,
    CONSTRAINT chk_baseball_play_event_inning_half CHECK (inning_half IN ('TOP', 'BOTTOM')),
    CONSTRAINT chk_baseball_play_event_event_type CHECK (event_type IN (
        'PLATE_APPEARANCE', 'SINGLE', 'DOUBLE', 'TRIPLE', 'HOME_RUN', 'WALK',
        'STRIKEOUT', 'OUT', 'ERROR', 'HIT_BY_PITCH', 'SACRIFICE_FLY', 'SACRIFICE_BUNT',
        'STOLEN_BASE', 'CAUGHT_STEALING', 'DOUBLE_PLAY', 'TRIPLE_PLAY', 'RUN_SCORED',
        'PITCHING_CHANGE', 'DEFENSIVE_CHANGE', 'COMMENT'
    ))
);

CREATE INDEX idx_baseball_play_event_match_id ON baseball_play_event(match_id);

CREATE TRIGGER trigger_set_updated_at_baseball_play_event
BEFORE UPDATE ON baseball_play_event
FOR EACH ROW
EXECUTE FUNCTION set_updated_at();
