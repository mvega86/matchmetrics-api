-- V45: Allow players to participate in multiple sports/teams
-- Creates a junction table player_teams and migrates existing team_id data into it.

CREATE TABLE player_teams (
    player_id BIGINT NOT NULL,
    team_id   BIGINT NOT NULL,
    PRIMARY KEY (player_id, team_id),
    CONSTRAINT fk_pt_player FOREIGN KEY (player_id) REFERENCES players(id) ON DELETE CASCADE,
    CONSTRAINT fk_pt_team   FOREIGN KEY (team_id)   REFERENCES teams(id)   ON DELETE CASCADE
);

-- Migrate existing single-team assignment into the junction table
INSERT INTO player_teams (player_id, team_id)
SELECT id, team_id
FROM players
WHERE team_id IS NOT NULL;
