-- V52: Índices en claves foráneas críticas + CHECK constraints en app_users

-- ── match ──────────────────────────────────────────────────────────────────
CREATE INDEX IF NOT EXISTS idx_match_home_team ON match(home_team_id);
CREATE INDEX IF NOT EXISTS idx_match_away_team ON match(away_team_id);

-- ── player_match ───────────────────────────────────────────────────────────
CREATE INDEX IF NOT EXISTS idx_player_match_match  ON player_match(match_id);
CREATE INDEX IF NOT EXISTS idx_player_match_player ON player_match(player_id);

-- ── player_statistic (renombrada desde match_statistic en V8) ──────────────
CREATE INDEX IF NOT EXISTS idx_player_statistic_player_match ON player_statistic(player_match_id);

-- ── player_teams (junction table — V45) ───────────────────────────────────
CREATE INDEX IF NOT EXISTS idx_player_teams_player ON player_teams(player_id);
CREATE INDEX IF NOT EXISTS idx_player_teams_team   ON player_teams(team_id);

-- ── app_users: CHECK constraints para role, status, provider ──────────────
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints
        WHERE constraint_name = 'chk_users_role' AND table_name = 'app_users'
    ) THEN
        ALTER TABLE app_users
            ADD CONSTRAINT chk_users_role
            CHECK (role IN ('ADMIN', 'MANAGER', 'USER'));
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints
        WHERE constraint_name = 'chk_users_status' AND table_name = 'app_users'
    ) THEN
        ALTER TABLE app_users
            ADD CONSTRAINT chk_users_status
            CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED', 'DISABLED'));
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints
        WHERE constraint_name = 'chk_users_provider' AND table_name = 'app_users'
    ) THEN
        ALTER TABLE app_users
            ADD CONSTRAINT chk_users_provider
            CHECK (provider IN ('LOCAL', 'GOOGLE', 'MICROSOFT', 'GITHUB'));
    END IF;
END $$;
