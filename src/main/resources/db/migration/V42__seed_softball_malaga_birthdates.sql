-- ============================================================
-- V42__seed_softball_malaga_birthdates.sql
-- Añade birth_date a todos los jugadores de softball
-- Fuente: AVG300 rosters PDF (exportado 13/06/2026)
-- Estrategia: UPDATE por (team_id, jersey_number) — sin
--   dependencia de nombre exacto, inmune a variaciones tipográficas.
-- ============================================================
--
-- ANOMALÍAS DOCUMENTADAS (no modificar comportamiento):
--   · EL #50 Alexander Perez R (1/29/2000): NO está en BD —
--     dorsal duplicado descartado por ON CONFLICT en V40.
--   · MARBELLA #31 Eduardo D Suarez R (3/20/1995): NO está en BD —
--     dorsal duplicado descartado por ON CONFLICT en V40.
--   · EL #39 Tim Spidel EL: DOB 11/10/2085 en PDF — probable error
--     tipográfico (¿1985?). Se importa tal cual; corregir manualmente.
--   · YANKEES "ricardo" x2 (6/13/2026): registros placeholder, no importados.
--   · YANKEES #25 ghost row (6/13/2026): sin nombre, no importado.
--   · TIGRES ghost row (6/13/2026): sin nombre, no importado.
--   · TOROS ghost row (6/13/2026): sin nombre, no importado.
--   · Hector A Idrogo F (#24) y Hector A Idrogo R (#99) son personas
--     distintas: DOBs 2/04/2011 y 8/23/1988 respectivamente.
-- ============================================================

-- ── 1. Jugadores CON dorsal ───────────────────────────────────
-- Coincidencia por (team_id, jersey_number); la constraint
-- unique_jersey_per_team garantiza que el par es único en BD.

UPDATE players p
SET birth_date = sub.dob
FROM (
    SELECT t.id AS team_id, v.jersey_number, v.dob::date AS dob
    FROM (VALUES
        -- ── AGUILAS DE MIJAS ──────────────────────────────────
        ('AGUILAS DE MIJAS',       1,  '1993-12-11'),
        ('AGUILAS DE MIJAS',       2,  '2003-01-08'),
        ('AGUILAS DE MIJAS',       5,  '1989-12-11'),
        ('AGUILAS DE MIJAS',       7,  '1976-08-04'),
        ('AGUILAS DE MIJAS',      10,  '1992-08-18'),
        ('AGUILAS DE MIJAS',      12,  '1978-12-07'),
        ('AGUILAS DE MIJAS',      13,  '1982-01-28'),
        ('AGUILAS DE MIJAS',      14,  '1996-10-27'),
        ('AGUILAS DE MIJAS',      15,  '1986-12-02'),
        ('AGUILAS DE MIJAS',      18,  '2003-05-28'),
        ('AGUILAS DE MIJAS',      19,  '1994-04-22'),
        ('AGUILAS DE MIJAS',      22,  '1994-03-29'),
        ('AGUILAS DE MIJAS',      23,  '2004-07-09'),
        ('AGUILAS DE MIJAS',      24,  '1991-03-22'),
        ('AGUILAS DE MIJAS',      26,  '1992-11-11'),
        ('AGUILAS DE MIJAS',      27,  '1993-02-02'),
        ('AGUILAS DE MIJAS',      28,  '2004-12-28'),
        ('AGUILAS DE MIJAS',      37,  '1994-09-19'),
        ('AGUILAS DE MIJAS',      38,  '1996-06-20'),
        ('AGUILAS DE MIJAS',      50,  '2004-06-14'),
        -- ── ASTROS DE MARBELLA ────────────────────────────────
        ('ASTROS DE MARBELLA',     0,  '1991-03-25'),
        ('ASTROS DE MARBELLA',     1,  '1992-01-14'),
        ('ASTROS DE MARBELLA',     2,  '1999-07-06'),
        ('ASTROS DE MARBELLA',     3,  '1985-06-30'),
        ('ASTROS DE MARBELLA',     6,  '1996-01-02'),
        ('ASTROS DE MARBELLA',     8,  '2004-03-23'),
        ('ASTROS DE MARBELLA',     9,  '1991-03-25'),
        ('ASTROS DE MARBELLA',    11,  '1988-08-20'),
        ('ASTROS DE MARBELLA',    13,  '1988-06-27'),
        ('ASTROS DE MARBELLA',    16,  '1989-07-19'),
        ('ASTROS DE MARBELLA',    21,  '1998-02-21'),
        ('ASTROS DE MARBELLA',    23,  '1994-02-02'),
        ('ASTROS DE MARBELLA',    24,  '1991-01-15'),
        ('ASTROS DE MARBELLA',    25,  '1991-01-20'),
        ('ASTROS DE MARBELLA',    26,  '1986-02-09'),
        ('ASTROS DE MARBELLA',    27,  '2003-01-14'),
        ('ASTROS DE MARBELLA',    99,  '2003-02-02'),
        -- ── ESTRELLA LATINA ───────────────────────────────────
        -- NOTA: #50 Alexander Perez R (1/29/2000) NO está en BD.
        -- NOTA: #39 Tim Spidel EL tiene DOB 11/10/2085 en PDF (posible error).
        ('ESTRELLA LATINA',        1,  '1975-01-31'),
        ('ESTRELLA LATINA',        2,  '1997-01-28'),
        ('ESTRELLA LATINA',        3,  '1989-04-19'),
        ('ESTRELLA LATINA',        4,  '1994-04-14'),
        ('ESTRELLA LATINA',        5,  '1999-11-12'),
        ('ESTRELLA LATINA',        6,  '1982-11-07'),
        ('ESTRELLA LATINA',        7,  '1978-11-27'),
        ('ESTRELLA LATINA',        8,  '1972-09-28'),
        ('ESTRELLA LATINA',        9,  '1976-06-09'),
        ('ESTRELLA LATINA',       10,  '1980-06-24'),
        ('ESTRELLA LATINA',       13,  '1978-03-11'),
        ('ESTRELLA LATINA',       16,  '1998-09-28'),
        ('ESTRELLA LATINA',       17,  '1988-09-21'),
        ('ESTRELLA LATINA',       18,  '1993-01-20'),
        ('ESTRELLA LATINA',       19,  '1989-09-19'),
        ('ESTRELLA LATINA',       20,  '1974-08-14'),
        ('ESTRELLA LATINA',       22,  '1981-02-03'),
        ('ESTRELLA LATINA',       23,  '1993-10-23'),
        ('ESTRELLA LATINA',       24,  '1995-10-08'),
        ('ESTRELLA LATINA',       27,  '1981-02-27'),
        ('ESTRELLA LATINA',       29,  '1981-07-29'),
        ('ESTRELLA LATINA',       31,  '2010-02-02'),
        ('ESTRELLA LATINA',       32,  '1965-11-11'),
        ('ESTRELLA LATINA',       34,  '1994-08-09'),
        ('ESTRELLA LATINA',       35,  '1978-11-13'),
        ('ESTRELLA LATINA',       38,  '2005-01-09'),
        ('ESTRELLA LATINA',       39,  '2085-11-10'),  -- DOB 2085 en PDF; probable error tipográfico
        ('ESTRELLA LATINA',       40,  '1983-05-27'),
        ('ESTRELLA LATINA',       44,  '1965-01-12'),
        ('ESTRELLA LATINA',       50,  '1985-09-16'),  -- Hector Pena P; Alexander Perez R no está en BD
        ('ESTRELLA LATINA',       56,  '1985-05-18'),
        ('ESTRELLA LATINA',       70,  '2007-01-24'),
        ('ESTRELLA LATINA',       72,  '2005-02-27'),
        ('ESTRELLA LATINA',       88,  '1994-05-24'),
        ('ESTRELLA LATINA',       89,  '1981-07-07'),
        ('ESTRELLA LATINA',       92,  '1994-11-02'),
        -- ── LOS ANGELES DE MALAGA ─────────────────────────────
        -- Hector A Idrogo F (#24, DOB 2011) y R (#99, DOB 1988) son personas distintas
        ('LOS ANGELES DE MALAGA',  0,  '1992-01-18'),
        ('LOS ANGELES DE MALAGA',  1,  '1991-03-05'),
        ('LOS ANGELES DE MALAGA',  3,  '1993-09-27'),
        ('LOS ANGELES DE MALAGA',  6,  '1990-03-31'),
        ('LOS ANGELES DE MALAGA',  7,  '1989-12-08'),
        ('LOS ANGELES DE MALAGA',  8,  '1992-12-09'),
        ('LOS ANGELES DE MALAGA', 13,  '2001-02-25'),
        ('LOS ANGELES DE MALAGA', 16,  '2003-07-13'),
        ('LOS ANGELES DE MALAGA', 17,  '1982-12-05'),
        ('LOS ANGELES DE MALAGA', 18,  '1979-01-07'),
        ('LOS ANGELES DE MALAGA', 20,  '1992-05-10'),
        ('LOS ANGELES DE MALAGA', 21,  '1983-11-18'),
        ('LOS ANGELES DE MALAGA', 22,  '2009-11-23'),
        ('LOS ANGELES DE MALAGA', 24,  '2011-02-04'),
        ('LOS ANGELES DE MALAGA', 27,  '1985-10-27'),
        ('LOS ANGELES DE MALAGA', 28,  '2004-08-28'),
        ('LOS ANGELES DE MALAGA', 33,  '1980-03-04'),
        ('LOS ANGELES DE MALAGA', 34,  '1994-08-27'),
        ('LOS ANGELES DE MALAGA', 41,  '1985-09-26'),
        ('LOS ANGELES DE MALAGA', 54,  '1973-07-05'),
        ('LOS ANGELES DE MALAGA', 99,  '1988-08-23'),
        -- ── MARBELLA ──────────────────────────────────────────
        -- NOTA: #31 Eduardo D Suarez R (3/20/1995) NO está en BD.
        ('MARBELLA',               1,  '1982-04-25'),
        ('MARBELLA',               5,  '1974-12-26'),
        ('MARBELLA',               8,  '1988-02-11'),
        ('MARBELLA',              12,  '2004-12-22'),
        ('MARBELLA',              14,  '1964-01-17'),
        ('MARBELLA',              15,  '1984-06-11'),
        ('MARBELLA',              19,  '1988-09-19'),
        ('MARBELLA',              20,  '1978-02-01'),
        ('MARBELLA',              21,  '1992-08-24'),
        ('MARBELLA',              22,  '1998-06-13'),
        ('MARBELLA',              23,  '2002-12-19'),
        ('MARBELLA',              24,  '2001-03-01'),
        ('MARBELLA',              27,  '1989-04-15'),
        ('MARBELLA',              31,  '1999-01-23'),  -- Junior D Gomez L; Eduardo D Suarez R no está en BD
        ('MARBELLA',              34,  '1970-02-14'),
        ('MARBELLA',              47,  '2002-05-13'),
        ('MARBELLA',              51,  '1993-02-06'),
        ('MARBELLA',              56,  '1964-08-26'),
        ('MARBELLA',              88,  '2000-04-06'),
        ('MARBELLA',              93,  '1993-03-31'),
        ('MARBELLA',              99,  '1997-10-29'),
        -- ── MARLINS DE MARBELLA ───────────────────────────────
        ('MARLINS DE MARBELLA',    0,  '2004-05-18'),
        ('MARLINS DE MARBELLA',    1,  '1981-12-10'),
        ('MARLINS DE MARBELLA',    2,  '1996-02-10'),
        ('MARLINS DE MARBELLA',    3,  '1986-03-15'),
        ('MARLINS DE MARBELLA',    4,  '1995-11-21'),
        ('MARLINS DE MARBELLA',    5,  '1985-04-01'),
        ('MARLINS DE MARBELLA',    6,  '1990-11-27'),
        ('MARLINS DE MARBELLA',    7,  '1974-08-01'),
        ('MARLINS DE MARBELLA',    8,  '2001-12-18'),
        ('MARLINS DE MARBELLA',    9,  '2001-02-04'),
        ('MARLINS DE MARBELLA',   12,  '1990-10-14'),
        ('MARLINS DE MARBELLA',   13,  '2002-10-04'),
        ('MARLINS DE MARBELLA',   15,  '1990-08-14'),
        ('MARLINS DE MARBELLA',   16,  '1979-09-14'),
        ('MARLINS DE MARBELLA',   19,  '2003-06-19'),
        ('MARLINS DE MARBELLA',   21,  '2005-04-13'),
        ('MARLINS DE MARBELLA',   23,  '1982-10-26'),
        ('MARLINS DE MARBELLA',   26,  '1991-06-26'),
        ('MARLINS DE MARBELLA',   31,  '2007-02-17'),
        ('MARLINS DE MARBELLA',   33,  '1990-05-14'),
        ('MARLINS DE MARBELLA',   34,  '1986-11-06'),
        ('MARLINS DE MARBELLA',   39,  '1994-06-07'),
        ('MARLINS DE MARBELLA',   44,  '1977-06-21'),
        ('MARLINS DE MARBELLA',   45,  '1986-06-24'),
        ('MARLINS DE MARBELLA',   60,  '1980-03-29'),
        ('MARLINS DE MARBELLA',   64,  '2003-09-15'),
        ('MARLINS DE MARBELLA',   66,  '1999-06-05'),
        ('MARLINS DE MARBELLA',   76,  '1976-05-10'),
        ('MARLINS DE MARBELLA',   80,  '1984-03-03'),
        ('MARLINS DE MARBELLA',   89,  '1991-10-15'),
        ('MARLINS DE MARBELLA',   90,  '1990-11-14'),
        ('MARLINS DE MARBELLA',   99,  '1977-11-28'),
        -- ── RANGERS DE TEXAS ──────────────────────────────────
        ('RANGERS DE TEXAS',       0,  '1989-08-19'),
        ('RANGERS DE TEXAS',       3,  '1998-01-13'),
        ('RANGERS DE TEXAS',       6,  '1987-03-09'),
        ('RANGERS DE TEXAS',       7,  '1982-12-20'),
        ('RANGERS DE TEXAS',       8,  '1989-08-08'),
        ('RANGERS DE TEXAS',       9,  '1984-05-22'),
        ('RANGERS DE TEXAS',      10,  '1993-02-02'),
        ('RANGERS DE TEXAS',      11,  '1993-04-25'),
        ('RANGERS DE TEXAS',      12,  '1989-01-27'),
        ('RANGERS DE TEXAS',      13,  '1998-01-11'),
        ('RANGERS DE TEXAS',      17,  '1971-07-26'),
        ('RANGERS DE TEXAS',      19,  '1986-05-15'),
        ('RANGERS DE TEXAS',      22,  '1984-03-22'),
        ('RANGERS DE TEXAS',      25,  '2002-10-25'),
        ('RANGERS DE TEXAS',      28,  '1985-07-26'),
        ('RANGERS DE TEXAS',      31,  '1984-02-11'),
        ('RANGERS DE TEXAS',      35,  '1978-01-18'),
        ('RANGERS DE TEXAS',      51,  '1981-09-16'),
        ('RANGERS DE TEXAS',      79,  '1994-09-01'),
        -- ── TIGRES DE MARBELLA ────────────────────────────────
        ('TIGRES DE MARBELLA',     0,  '1986-05-17'),
        ('TIGRES DE MARBELLA',     1,  '1997-03-22'),
        ('TIGRES DE MARBELLA',     4,  '1996-04-22'),
        ('TIGRES DE MARBELLA',     9,  '1986-02-28'),
        ('TIGRES DE MARBELLA',    10,  '1989-06-15'),
        ('TIGRES DE MARBELLA',    11,  '1977-05-09'),
        ('TIGRES DE MARBELLA',    12,  '1982-04-27'),
        ('TIGRES DE MARBELLA',    14,  '1997-05-15'),
        ('TIGRES DE MARBELLA',    15,  '1979-09-02'),
        ('TIGRES DE MARBELLA',    16,  '2005-10-10'),
        ('TIGRES DE MARBELLA',    19,  '1997-04-28'),
        ('TIGRES DE MARBELLA',    21,  '1993-12-13'),
        ('TIGRES DE MARBELLA',    22,  '1998-12-29'),
        ('TIGRES DE MARBELLA',    23,  '2010-06-16'),
        ('TIGRES DE MARBELLA',    27,  '2008-10-30'),
        ('TIGRES DE MARBELLA',    29,  '1990-07-25'),
        ('TIGRES DE MARBELLA',    31,  '1992-12-31'),
        ('TIGRES DE MARBELLA',    41,  '1983-09-22'),
        ('TIGRES DE MARBELLA',    45,  '1988-11-05'),
        ('TIGRES DE MARBELLA',    47,  '1991-11-06'),
        ('TIGRES DE MARBELLA',    99,  '1993-09-11'),
        -- ── TOROS DEL VISO ────────────────────────────────────
        ('TOROS DEL VISO',         2,  '2002-06-06'),
        ('TOROS DEL VISO',         3,  '2002-12-10'),
        ('TOROS DEL VISO',         5,  '1999-01-10'),
        ('TOROS DEL VISO',         7,  '1975-09-18'),
        ('TOROS DEL VISO',        10,  '2005-01-10'),
        ('TOROS DEL VISO',        11,  '1990-12-10'),
        ('TOROS DEL VISO',        17,  '1977-02-10'),
        ('TOROS DEL VISO',        18,  '1994-01-25'),
        ('TOROS DEL VISO',        20,  '2004-02-20'),
        ('TOROS DEL VISO',        23,  '1988-08-17'),
        ('TOROS DEL VISO',        24,  '1975-02-07'),
        ('TOROS DEL VISO',        25,  '1991-08-10'),
        ('TOROS DEL VISO',        26,  '1979-01-26'),
        ('TOROS DEL VISO',        28,  '2004-01-31'),
        ('TOROS DEL VISO',        33,  '1999-11-15'),
        ('TOROS DEL VISO',        53,  '2003-12-14'),
        ('TOROS DEL VISO',        61,  '1999-07-02'),
        ('TOROS DEL VISO',        88,  '1998-09-26'),
        ('TOROS DEL VISO',        89,  '1989-12-22'),
        ('TOROS DEL VISO',        99,  '1988-09-29'),
        -- ── VENEMALAGA ────────────────────────────────────────
        ('VENEMALAGA',             8,  '1972-01-17'),
        ('VENEMALAGA',             9,  '1965-05-09'),
        ('VENEMALAGA',            13,  '1997-08-13'),
        ('VENEMALAGA',            14,  '1985-12-07'),
        ('VENEMALAGA',            17,  '1987-09-17'),
        ('VENEMALAGA',            19,  '1991-05-25'),
        ('VENEMALAGA',            21,  '1983-08-28'),
        ('VENEMALAGA',            22,  '1981-10-17'),
        ('VENEMALAGA',            23,  '2005-09-17'),
        ('VENEMALAGA',            27,  '2001-06-06'),
        ('VENEMALAGA',            28,  '1992-02-20'),
        ('VENEMALAGA',            31,  '1978-09-18'),
        ('VENEMALAGA',            33,  '1987-07-18'),
        ('VENEMALAGA',            37,  '1987-05-12'),
        ('VENEMALAGA',            57,  '1988-03-22'),
        ('VENEMALAGA',            77,  '1971-10-16'),
        ('VENEMALAGA',            88,  '1988-12-15'),
        -- ── YANKEES DE MALAGA ─────────────────────────────────
        -- jersey_number=8 es Edward D Montilla G (corregido en V41 desde #6)
        -- #25: solo Ricardo R Natera D está en BD; ghost row 6/13/2026 no importado
        ('YANKEES DE MALAGA',      0,  '1977-10-25'),
        ('YANKEES DE MALAGA',      1,  '1996-09-03'),
        ('YANKEES DE MALAGA',      2,  '1969-09-08'),
        ('YANKEES DE MALAGA',      3,  '1998-01-19'),
        ('YANKEES DE MALAGA',      4,  '1993-03-05'),
        ('YANKEES DE MALAGA',      8,  '1990-07-30'),
        ('YANKEES DE MALAGA',     11,  '1976-01-29'),
        ('YANKEES DE MALAGA',     13,  '2003-12-13'),
        ('YANKEES DE MALAGA',     15,  '2001-09-15'),
        ('YANKEES DE MALAGA',     16,  '1983-06-03'),
        ('YANKEES DE MALAGA',     19,  '1980-08-14'),
        ('YANKEES DE MALAGA',     21,  '1972-04-19'),
        ('YANKEES DE MALAGA',     22,  '1986-01-31'),
        ('YANKEES DE MALAGA',     23,  '1991-08-29'),
        ('YANKEES DE MALAGA',     24,  '2000-07-24'),
        ('YANKEES DE MALAGA',     25,  '1997-07-11'),
        ('YANKEES DE MALAGA',     26,  '1985-05-26'),
        ('YANKEES DE MALAGA',     27,  '1966-05-20'),
        ('YANKEES DE MALAGA',     32,  '1980-04-08'),
        ('YANKEES DE MALAGA',     33,  '1988-11-09'),
        ('YANKEES DE MALAGA',     43,  '2002-05-22'),
        ('YANKEES DE MALAGA',     44,  '1986-02-05'),
        ('YANKEES DE MALAGA',     67,  '1976-10-12'),
        ('YANKEES DE MALAGA',     77,  '1981-05-06'),
        ('YANKEES DE MALAGA',     99,  '2003-06-14')
    ) AS v(team_name, jersey_number, dob)
    JOIN teams t ON t.name = v.team_name AND t.sport_type = 'SOFTBALL'
) AS sub
WHERE p.team_id = sub.team_id
  AND p.jersey_number = sub.jersey_number;

-- ── 2. Jugadores SIN dorsal ───────────────────────────────────
-- Coincidencia por (team_id, full_name) dado que jersey_number es NULL.

UPDATE players p
SET birth_date = v.dob::date
FROM (VALUES
    ('MARBELLA',       'Heaklyff Y Cardenas F', '1977-12-06'),
    ('TOROS DEL VISO', 'Carlos A Pantaleon C',  '1967-08-03'),
    ('TOROS DEL VISO', 'Jose M Cordoba C',      '1987-05-11')
) AS v(team_name, full_name, dob)
JOIN teams t ON t.name = v.team_name AND t.sport_type = 'SOFTBALL'
WHERE p.team_id = t.id
  AND p.full_name = v.full_name
  AND p.jersey_number IS NULL;
