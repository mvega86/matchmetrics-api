CREATE TABLE tournament_team (
    tournament_id BIGINT NOT NULL,
    team_id       BIGINT NOT NULL,
    PRIMARY KEY (tournament_id, team_id),
    CONSTRAINT fk_tt_tournament FOREIGN KEY (tournament_id) REFERENCES tournament(id) ON DELETE CASCADE,
    CONSTRAINT fk_tt_team       FOREIGN KEY (team_id)       REFERENCES teams(id)      ON DELETE CASCADE
);

CREATE INDEX idx_tt_tournament ON tournament_team(tournament_id);
CREATE INDEX idx_tt_team       ON tournament_team(team_id);
