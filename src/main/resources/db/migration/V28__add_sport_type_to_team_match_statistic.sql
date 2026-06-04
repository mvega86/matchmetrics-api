-- Add sport_type to teams
ALTER TABLE teams ADD COLUMN sport_type VARCHAR(20) NOT NULL DEFAULT 'FOOTBALL';

-- Add sport_type to match
ALTER TABLE match ADD COLUMN sport_type VARCHAR(20) NOT NULL DEFAULT 'FOOTBALL';

-- Add sport_type to statistic
ALTER TABLE statistic ADD COLUMN sport_type VARCHAR(20) NOT NULL DEFAULT 'FOOTBALL';
