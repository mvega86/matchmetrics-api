-- ============================================================
-- SEED: Jugadoras para Tigres (TIG) y Leonas (LEO)
-- Partidos pendientes: 31 (TIG vs AGU), 32 (LEO vs AGU), 33 (TIG vs OSO)
-- Ejecutar DESPUÉS de seed_softball_players_oso_agu.sql
-- Ejecutar manualmente en la BD de desarrollo
-- ============================================================

DO $$
DECLARE
  v_tig    BIGINT;
  v_leo    BIGINT;
  v_agu    BIGINT;
  v_oso    BIGINT;
  v_m31    BIGINT;  -- TIG vs AGU
  v_m32    BIGINT;  -- LEO vs AGU
  v_m33    BIGINT;  -- TIG vs OSO

  -- Tigres (9 titulares + 2 suplentes)
  p_t1  BIGINT; p_t2  BIGINT; p_t3  BIGINT;
  p_t4  BIGINT; p_t5  BIGINT; p_t6  BIGINT;
  p_t7  BIGINT; p_t8  BIGINT; p_t9  BIGINT;
  p_t10 BIGINT; p_t11 BIGINT;

  -- Leonas (9 titulares + 2 suplentes)
  p_l1  BIGINT; p_l2  BIGINT; p_l3  BIGINT;
  p_l4  BIGINT; p_l5  BIGINT; p_l6  BIGINT;
  p_l7  BIGINT; p_l8  BIGINT; p_l9  BIGINT;
  p_l10 BIGINT; p_l11 BIGINT;

BEGIN
  -- ── Equipos ──────────────────────────────────────────────
  SELECT id INTO v_tig FROM teams WHERE acronym = 'TIG' AND sport_type = 'SOFTBALL' LIMIT 1;
  SELECT id INTO v_leo FROM teams WHERE acronym = 'LEO' AND sport_type = 'SOFTBALL' LIMIT 1;
  SELECT id INTO v_agu FROM teams WHERE acronym = 'AGU' AND sport_type = 'SOFTBALL' LIMIT 1;
  SELECT id INTO v_oso FROM teams WHERE acronym = 'OSO' AND sport_type = 'SOFTBALL' LIMIT 1;

  -- ── Partidos ──────────────────────────────────────────────
  SELECT m.id INTO v_m31
    FROM match m
    JOIN teams ht ON ht.id = m.home_team_id
    JOIN teams at ON at.id = m.away_team_id
   WHERE ht.acronym = 'TIG' AND at.acronym = 'AGU'
     AND m.sport_type = 'SOFTBALL'
   LIMIT 1;

  SELECT m.id INTO v_m32
    FROM match m
    JOIN teams ht ON ht.id = m.home_team_id
    JOIN teams at ON at.id = m.away_team_id
   WHERE ht.acronym = 'LEO' AND at.acronym = 'AGU'
     AND m.sport_type = 'SOFTBALL'
   LIMIT 1;

  SELECT m.id INTO v_m33
    FROM match m
    JOIN teams ht ON ht.id = m.home_team_id
    JOIN teams at ON at.id = m.away_team_id
   WHERE ht.acronym = 'TIG' AND at.acronym = 'OSO'
     AND m.sport_type = 'SOFTBALL'
   LIMIT 1;

  RAISE NOTICE 'Equipos → TIG: %, LEO: %, AGU: %, OSO: %', v_tig, v_leo, v_agu, v_oso;
  RAISE NOTICE 'Partidos → M31: %, M32: %, M33: %', v_m31, v_m32, v_m33;

  -- ────────────────────────────────────────────────────────────
  -- TIGRES DEL CARIBE (TIG) — 9 titulares + 2 suplentes
  -- ────────────────────────────────────────────────────────────
  INSERT INTO players (full_name, jersey_name, jersey_number, birth_date)
  VALUES ('Fernanda Ruiz',       'Ruiz',       4,  '1999-03-15') RETURNING id INTO p_t1;
  INSERT INTO players (full_name, jersey_name, jersey_number, birth_date)
  VALUES ('Valentina Cruz',      'Cruz',       6,  '2000-07-22') RETURNING id INTO p_t2;
  INSERT INTO players (full_name, jersey_name, jersey_number, birth_date)
  VALUES ('Andrea López',        'López',      9,  '1998-11-08') RETURNING id INTO p_t3;
  INSERT INTO players (full_name, jersey_name, jersey_number, birth_date)
  VALUES ('María Fernández',     'Fernández',  13, '2001-04-30') RETURNING id INTO p_t4;
  INSERT INTO players (full_name, jersey_name, jersey_number, birth_date)
  VALUES ('Claudia Mendez',      'Mendez',     18, '1997-09-12') RETURNING id INTO p_t5;
  INSERT INTO players (full_name, jersey_name, jersey_number, birth_date)
  VALUES ('Paola Ramírez',       'Ramírez',    23, '2002-01-25') RETURNING id INTO p_t6;
  INSERT INTO players (full_name, jersey_name, jersey_number, birth_date)
  VALUES ('Cristina Soto',       'Soto',       29, '1996-06-18') RETURNING id INTO p_t7;
  INSERT INTO players (full_name, jersey_name, jersey_number, birth_date)
  VALUES ('Isabella Navarro',    'Navarro',    34, '2003-08-05') RETURNING id INTO p_t8;
  INSERT INTO players (full_name, jersey_name, jersey_number, birth_date)
  VALUES ('Lucía Guerrero',      'Guerrero',   41, '1995-12-20') RETURNING id INTO p_t9;
  -- Suplentes
  INSERT INTO players (full_name, jersey_name, jersey_number, birth_date)
  VALUES ('Gabriela Medina',     'Medina',     16, '2001-05-14') RETURNING id INTO p_t10;
  INSERT INTO players (full_name, jersey_name, jersey_number, birth_date)
  VALUES ('Daniela Vargas',      'Vargas',     38, '2000-02-09') RETURNING id INTO p_t11;

  UPDATE players SET team_id = v_tig
   WHERE id IN (p_t1,p_t2,p_t3,p_t4,p_t5,p_t6,p_t7,p_t8,p_t9,p_t10,p_t11);

  -- ────────────────────────────────────────────────────────────
  -- LEONAS DEL PACÍFICO (LEO) — 9 titulares + 2 suplentes
  -- ────────────────────────────────────────────────────────────
  INSERT INTO players (full_name, jersey_name, jersey_number, birth_date)
  VALUES ('Sofía Ibáñez',        'Ibáñez',     2,  '1999-06-03') RETURNING id INTO p_l1;
  INSERT INTO players (full_name, jersey_name, jersey_number, birth_date)
  VALUES ('Natalia Rojas',       'Rojas',      5,  '2001-10-17') RETURNING id INTO p_l2;
  INSERT INTO players (full_name, jersey_name, jersey_number, birth_date)
  VALUES ('Alejandra Fuentes',   'Fuentes',    10, '1998-02-28') RETURNING id INTO p_l3;
  INSERT INTO players (full_name, jersey_name, jersey_number, birth_date)
  VALUES ('Carolina Delgado',    'Delgado',    14, '2000-08-11') RETURNING id INTO p_l4;
  INSERT INTO players (full_name, jersey_name, jersey_number, birth_date)
  VALUES ('Mónica Castañeda',    'Castañeda',  19, '1997-04-06') RETURNING id INTO p_l5;
  INSERT INTO players (full_name, jersey_name, jersey_number, birth_date)
  VALUES ('Juliana Ríos',        'Ríos',       25, '2002-12-01') RETURNING id INTO p_l6;
  INSERT INTO players (full_name, jersey_name, jersey_number, birth_date)
  VALUES ('Marcela Gutiérrez',   'Gutiérrez',  30, '1996-07-24') RETURNING id INTO p_l7;
  INSERT INTO players (full_name, jersey_name, jersey_number, birth_date)
  VALUES ('Sandra Mora',         'Mora',       37, '2003-03-13') RETURNING id INTO p_l8;
  INSERT INTO players (full_name, jersey_name, jersey_number, birth_date)
  VALUES ('Patricia Suárez',     'Suárez',     43, '1994-11-29') RETURNING id INTO p_l9;
  -- Suplentes
  INSERT INTO players (full_name, jersey_name, jersey_number, birth_date)
  VALUES ('Diana Hernández',     'Hernández',  11, '2001-09-07') RETURNING id INTO p_l10;
  INSERT INTO players (full_name, jersey_name, jersey_number, birth_date)
  VALUES ('Elena Zapata',        'Zapata',     26, '2000-04-22') RETURNING id INTO p_l11;

  UPDATE players SET team_id = v_leo
   WHERE id IN (p_l1,p_l2,p_l3,p_l4,p_l5,p_l6,p_l7,p_l8,p_l9,p_l10,p_l11);

  -- ────────────────────────────────────────────────────────────
  -- PARTIDO 31: TIG (local) vs AGU (visitante)
  -- ────────────────────────────────────────────────────────────
  -- Orden │ Jugadora   │ Pos
  --   1   │ Cruz       │ SS
  --   2   │ Ruiz       │ 2B
  --   3   │ López      │ LF
  --   4   │ Mendez     │ 1B (cleanup)
  --   5   │ Fernández  │ 3B
  --   6   │ Ramírez    │ CF
  --   7   │ Soto       │ RF
  --   8   │ Navarro    │ C
  --   9   │ Guerrero   │ P
  INSERT INTO player_match (match_id, player_id, in_, batting_order, field_position)
  VALUES
    (v_m31, p_t2,  now(), 1, 'SS'),
    (v_m31, p_t1,  now(), 2, '2B'),
    (v_m31, p_t3,  now(), 3, 'LF'),
    (v_m31, p_t5,  now(), 4, '1B'),
    (v_m31, p_t4,  now(), 5, '3B'),
    (v_m31, p_t6,  now(), 6, 'CF'),
    (v_m31, p_t7,  now(), 7, 'RF'),
    (v_m31, p_t8,  now(), 8, 'C'),
    (v_m31, p_t9,  now(), 9, 'P');
  -- Suplentes TIG en M31
  INSERT INTO player_match (match_id, player_id, in_, batting_order, field_position)
  VALUES
    (v_m31, p_t10, NULL, NULL, NULL),
    (v_m31, p_t11, NULL, NULL, NULL);

  -- AGU players en M31 (suplentes — el manager AGU configura su alineación)
  INSERT INTO player_match (match_id, player_id, in_, batting_order, field_position)
  SELECT v_m31, id, NULL, NULL, NULL
    FROM players WHERE team_id = v_agu ORDER BY jersey_number;

  -- ────────────────────────────────────────────────────────────
  -- PARTIDO 32: LEO (local) vs AGU (visitante)
  -- ────────────────────────────────────────────────────────────
  -- Orden │ Jugadora    │ Pos
  --   1   │ Rojas       │ CF
  --   2   │ Ibáñez      │ SS
  --   3   │ Fuentes     │ 3B
  --   4   │ Castañeda   │ 1B (cleanup)
  --   5   │ Delgado     │ LF
  --   6   │ Ríos        │ 2B
  --   7   │ Gutiérrez   │ RF
  --   8   │ Mora        │ C
  --   9   │ Suárez      │ P
  INSERT INTO player_match (match_id, player_id, in_, batting_order, field_position)
  VALUES
    (v_m32, p_l2,  now(), 1, 'CF'),
    (v_m32, p_l1,  now(), 2, 'SS'),
    (v_m32, p_l3,  now(), 3, '3B'),
    (v_m32, p_l5,  now(), 4, '1B'),
    (v_m32, p_l4,  now(), 5, 'LF'),
    (v_m32, p_l6,  now(), 6, '2B'),
    (v_m32, p_l7,  now(), 7, 'RF'),
    (v_m32, p_l8,  now(), 8, 'C'),
    (v_m32, p_l9,  now(), 9, 'P');
  -- Suplentes LEO en M32
  INSERT INTO player_match (match_id, player_id, in_, batting_order, field_position)
  VALUES
    (v_m32, p_l10, NULL, NULL, NULL),
    (v_m32, p_l11, NULL, NULL, NULL);

  -- AGU players en M32
  INSERT INTO player_match (match_id, player_id, in_, batting_order, field_position)
  SELECT v_m32, id, NULL, NULL, NULL
    FROM players WHERE team_id = v_agu ORDER BY jersey_number;

  -- ────────────────────────────────────────────────────────────
  -- PARTIDO 33: TIG (local) vs OSO (visitante)
  -- ────────────────────────────────────────────────────────────
  -- TIG con misma alineación que M31
  INSERT INTO player_match (match_id, player_id, in_, batting_order, field_position)
  VALUES
    (v_m33, p_t2,  now(), 1, 'SS'),
    (v_m33, p_t1,  now(), 2, '2B'),
    (v_m33, p_t3,  now(), 3, 'LF'),
    (v_m33, p_t5,  now(), 4, '1B'),
    (v_m33, p_t4,  now(), 5, '3B'),
    (v_m33, p_t6,  now(), 6, 'CF'),
    (v_m33, p_t7,  now(), 7, 'RF'),
    (v_m33, p_t8,  now(), 8, 'C'),
    (v_m33, p_t9,  now(), 9, 'P');
  -- Suplentes TIG en M33
  INSERT INTO player_match (match_id, player_id, in_, batting_order, field_position)
  VALUES
    (v_m33, p_t10, NULL, NULL, NULL),
    (v_m33, p_t11, NULL, NULL, NULL);

  -- OSO players en M33
  INSERT INTO player_match (match_id, player_id, in_, batting_order, field_position)
  SELECT v_m33, id, NULL, NULL, NULL
    FROM players WHERE team_id = v_oso ORDER BY jersey_number;

  RAISE NOTICE 'Seed completado: 22 jugadoras TIG+LEO creadas, alineaciones para M31/M32/M33 insertadas';
END $$;
