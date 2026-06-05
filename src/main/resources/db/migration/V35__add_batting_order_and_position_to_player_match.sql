-- Add softball/baseball lineup fields to player_match
-- batting_order: 1-9 (or 10 for slow-pitch), NULL = bench
-- field_position: P, C, 1B, 2B, 3B, SS, LF, CF, RF, EH, BN
ALTER TABLE player_match
    ADD COLUMN IF NOT EXISTS batting_order  SMALLINT     CHECK (batting_order >= 1 AND batting_order <= 10),
    ADD COLUMN IF NOT EXISTS field_position VARCHAR(10);
