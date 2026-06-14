-- ============================================================
-- V41__seed_softball_malaga_fix.sql
-- Completa el seed de V40 con los 21 jugadores omitidos por
-- corte de página en la extracción del PDF de rosters AVG300.
-- Corrige además un error de dorsal/inicial en Yankees #6→#8.
-- Flyway garantiza ejecución única; ON CONFLICT DO NOTHING
-- protege ante doble ejecución accidental.
-- ============================================================

-- ── Corrección: Yankees #6 era error de extracción → dorsal 8 ─
UPDATE players
SET jersey_number = 8,
    full_name     = 'Edward D Montilla G'
WHERE full_name  = 'Edward D Montilla C'
  AND jersey_number = 6
  AND team_id = (
      SELECT id FROM teams
      WHERE name = 'YANKEES DE MALAGA' AND sport_type = 'SOFTBALL'
  );

-- ── Jugadores faltantes ────────────────────────────────────────
INSERT INTO players (full_name, jersey_number, team_id)
SELECT v.full_name, v.jersey_number, t.id
FROM (VALUES
    -- ASTROS DE MARBELLA (4 faltantes: dorsales 24-27)
    ('ASTROS DE MARBELLA',    'Cipriano D Castro',         24),
    ('ASTROS DE MARBELLA',    'Vicni Pereira AM',          25),
    ('ASTROS DE MARBELLA',    'Larry HALUM AM',            26),
    ('ASTROS DE MARBELLA',    'Gabriel A Pacheco',         27),
    -- ESTRELLA LATINA (2 faltantes: dorsales 89 y 92)
    ('ESTRELLA LATINA',       'Werlin R Reynoso',          89),
    ('ESTRELLA LATINA',       'Raidel Crespo H',           92),
    -- LOS ANGELES DE MALAGA (3 faltantes: dorsales 34, 41, 54)
    ('LOS ANGELES DE MALAGA', 'Daniel A Valdes A',         34),
    ('LOS ANGELES DE MALAGA', 'Douglas A Rodriguez P',     41),
    ('LOS ANGELES DE MALAGA', 'Gerardo A Carmona',         54),
    -- MARLINS DE MARBELLA (3 faltantes: dorsales 9, 12, 13)
    ('MARLINS DE MARBELLA',   'Ferlin Terrero Novas',       9),
    ('MARLINS DE MARBELLA',   'Jason J Matos P',           12),
    ('MARLINS DE MARBELLA',   'Roniel D Rodriguez R',      13),
    -- RANGERS DE TEXAS (3 faltantes: dorsales 31, 35, 51)
    ('RANGERS DE TEXAS',      'Adael Rosales',             31),
    ('RANGERS DE TEXAS',      'Edgar J Perez G',           35),
    ('RANGERS DE TEXAS',      'Carlos L Ruiz',             51),
    -- TOROS DEL VISO (3 faltantes: dorsales 24, 25, 26)
    ('TOROS DEL VISO',        'Imber J Gutierrez T',       24),
    ('TOROS DEL VISO',        'Gabriel Vivas',             25),
    ('TOROS DEL VISO',        'Luis M Moreno R',           26),
    -- YANKEES DE MALAGA (3 faltantes: dorsales 11, 13, 15)
    ('YANKEES DE MALAGA',     'Antonio Mendez V',          11),
    ('YANKEES DE MALAGA',     'Eddy R Diaz L',             13),
    ('YANKEES DE MALAGA',     'Alexis A Rangel M',         15)
) AS v(team_name, full_name, jersey_number)
JOIN teams t ON t.name = v.team_name AND t.sport_type = 'SOFTBALL'
ON CONFLICT (team_id, jersey_number) DO NOTHING;
