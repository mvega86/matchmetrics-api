ALTER TABLE baseball_game_state
    ADD COLUMN current_batter_player_match_id BIGINT,
    ADD CONSTRAINT fk_baseball_gs_current_batter
        FOREIGN KEY (current_batter_player_match_id)
        REFERENCES player_match(id)
        ON DELETE SET NULL;
