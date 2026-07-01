-- V53: Performance indexes for high-frequency query paths

-- ── baseball_play_event: FK columns missing from V27 ─────────────────────────
-- These columns are the primary filter axes in PlayerStatisticsQueryService.
CREATE INDEX IF NOT EXISTS idx_bpe_batter_pm       ON baseball_play_event(batter_player_match_id);
CREATE INDEX IF NOT EXISTS idx_bpe_pitcher_pm      ON baseball_play_event(pitcher_player_match_id);
CREATE INDEX IF NOT EXISTS idx_bpe_batting_team    ON baseball_play_event(batting_team_id);
CREATE INDEX IF NOT EXISTS idx_bpe_fielding_team   ON baseball_play_event(fielding_team_id);

-- Composite index covering the most common stats query: sport_type filter on the match side.
-- Queries like findAllBattingEventsBySportType join baseball_play_event → match on match_id,
-- then filter WHERE match.sport_type = ?; this index covers that filter on the match table.
CREATE INDEX IF NOT EXISTS idx_match_sport_type    ON match(sport_type);
CREATE INDEX IF NOT EXISTS idx_match_tournament_id ON match(tournament_id);

-- ── player_statistic: statistic_id FK index ──────────────────────────────────
CREATE INDEX IF NOT EXISTS idx_player_statistic_statistic ON player_statistic(statistic_id);
