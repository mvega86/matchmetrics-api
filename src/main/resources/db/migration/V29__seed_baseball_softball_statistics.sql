-- Replace unique constraint on name with composite (name, sport_type)
-- so the same abbreviation can exist for different sports
ALTER TABLE statistic DROP CONSTRAINT IF EXISTS statistic_name_key;
ALTER TABLE statistic ADD CONSTRAINT uq_statistic_name_sport UNIQUE (name, sport_type);

-- =============================================
-- BASEBALL statistics
-- =============================================

-- Bateo
INSERT INTO statistic (name, description, unit, sport_type) VALUES
    ('AB',      'At Bats',                  NULL,   'BASEBALL'),
    ('R',       'Runs',                     NULL,   'BASEBALL'),
    ('H',       'Hits',                     NULL,   'BASEBALL'),
    ('RBI',     'Runs Batted In',           NULL,   'BASEBALL'),
    ('BB',      'Base on Balls (Walk)',      NULL,   'BASEBALL'),
    ('SO',      'Strikeouts (Batter)',       NULL,   'BASEBALL'),
    ('1B',      'Singles',                  NULL,   'BASEBALL'),
    ('2B',      'Doubles',                  NULL,   'BASEBALL'),
    ('3B',      'Triples',                  NULL,   'BASEBALL'),
    ('HR',      'Home Runs',                NULL,   'BASEBALL'),
    ('SB',      'Stolen Bases',             NULL,   'BASEBALL'),
    ('CS',      'Caught Stealing',          NULL,   'BASEBALL'),
-- Pitcheo
    ('IP',      'Innings Pitched',          NULL,   'BASEBALL'),
    ('HA',      'Hits Allowed',             NULL,   'BASEBALL'),
    ('RA',      'Runs Allowed',             NULL,   'BASEBALL'),
    ('ER',      'Earned Runs',              NULL,   'BASEBALL'),
    ('BBA',     'Walks Allowed',            NULL,   'BASEBALL'),
    ('SOA',     'Strikeouts (Pitcher)',      NULL,   'BASEBALL'),
    ('HRA',     'Home Runs Allowed',        NULL,   'BASEBALL'),
    ('Pitches', 'Total Pitches',            NULL,   'BASEBALL'),
    ('Strikes', 'Total Strikes',            NULL,   'BASEBALL'),
-- Defensa
    ('E',       'Errors',                   NULL,   'BASEBALL'),
    ('PO',      'Putouts',                  NULL,   'BASEBALL'),
    ('A',       'Assists',                  NULL,   'BASEBALL'),
    ('DP',      'Double Plays',             NULL,   'BASEBALL');

-- =============================================
-- SOFTBALL statistics (same structure as baseball)
-- =============================================

-- Bateo
INSERT INTO statistic (name, description, unit, sport_type) VALUES
    ('AB',      'At Bats',                  NULL,   'SOFTBALL'),
    ('R',       'Runs',                     NULL,   'SOFTBALL'),
    ('H',       'Hits',                     NULL,   'SOFTBALL'),
    ('RBI',     'Runs Batted In',           NULL,   'SOFTBALL'),
    ('BB',      'Base on Balls (Walk)',      NULL,   'SOFTBALL'),
    ('SO',      'Strikeouts (Batter)',       NULL,   'SOFTBALL'),
    ('1B',      'Singles',                  NULL,   'SOFTBALL'),
    ('2B',      'Doubles',                  NULL,   'SOFTBALL'),
    ('3B',      'Triples',                  NULL,   'SOFTBALL'),
    ('HR',      'Home Runs',                NULL,   'SOFTBALL'),
    ('SB',      'Stolen Bases',             NULL,   'SOFTBALL'),
    ('CS',      'Caught Stealing',          NULL,   'SOFTBALL'),
-- Pitcheo
    ('IP',      'Innings Pitched',          NULL,   'SOFTBALL'),
    ('HA',      'Hits Allowed',             NULL,   'SOFTBALL'),
    ('RA',      'Runs Allowed',             NULL,   'SOFTBALL'),
    ('ER',      'Earned Runs',              NULL,   'SOFTBALL'),
    ('BBA',     'Walks Allowed',            NULL,   'SOFTBALL'),
    ('SOA',     'Strikeouts (Pitcher)',      NULL,   'SOFTBALL'),
    ('HRA',     'Home Runs Allowed',        NULL,   'SOFTBALL'),
    ('Pitches', 'Total Pitches',            NULL,   'SOFTBALL'),
    ('Strikes', 'Total Strikes',            NULL,   'SOFTBALL'),
-- Defensa
    ('E',       'Errors',                   NULL,   'SOFTBALL'),
    ('PO',      'Putouts',                  NULL,   'SOFTBALL'),
    ('A',       'Assists',                  NULL,   'SOFTBALL'),
    ('DP',      'Double Plays',             NULL,   'SOFTBALL');
