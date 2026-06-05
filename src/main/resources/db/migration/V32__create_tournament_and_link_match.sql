CREATE TABLE tournament (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    description VARCHAR(500),
    sport_type  VARCHAR(50)  NOT NULL,
    status      VARCHAR(50)  NOT NULL DEFAULT 'UPCOMING',
    start_date  DATE,
    end_date    DATE,
    organizer   VARCHAR(255),
    location    VARCHAR(255),
    created_at  TIMESTAMP DEFAULT now(),
    updated_at  TIMESTAMP DEFAULT now(),
    modified_by VARCHAR(255),

    CONSTRAINT chk_tournament_status
        CHECK (status IN ('UPCOMING', 'IN_PROGRESS', 'FINISHED', 'CANCELLED')),
    CONSTRAINT chk_tournament_sport_type
        CHECK (sport_type IN ('BASEBALL', 'SOFTBALL', 'FOOTBALL'))
);

CREATE TRIGGER trigger_set_updated_at_tournament
BEFORE UPDATE ON tournament
FOR EACH ROW
EXECUTE FUNCTION set_updated_at();

ALTER TABLE match
    ADD COLUMN tournament_id BIGINT,
    ADD CONSTRAINT fk_match_tournament
        FOREIGN KEY (tournament_id) REFERENCES tournament(id)
        ON DELETE SET NULL;

CREATE INDEX idx_match_tournament_id ON match(tournament_id);
