-- ============================================================
-- SEED: Jugadoras para el partido OSO vs AGU (Copa Softball Femenina 2026)
-- Requiere que V35 ya esté aplicada
-- Ejecutar manualmente en la BD de desarrollo
-- ============================================================

DO $$
DECLARE
  v_oso    BIGINT;
  v_agu    BIGINT;
  v_match  BIGINT;

  -- IDs de jugadoras OSO
  p_oso1  BIGINT; p_oso2  BIGINT; p_oso3  BIGINT;
  p_oso4  BIGINT; p_oso5  BIGINT; p_oso6  BIGINT;
  p_oso7  BIGINT; p_oso8  BIGINT; p_oso9  BIGINT;

  -- IDs de jugadoras AGU
  p_agu1  BIGINT; p_agu2  BIGINT; p_agu3  BIGINT;
  p_agu4  BIGINT; p_agu5  BIGINT; p_agu6  BIGINT;
  p_agu7  BIGINT; p_agu8  BIGINT; p_agu9  BIGINT;

BEGIN
  -- ── Equipos ──────────────────────────────────────────────
  SELECT id INTO v_oso FROM teams WHERE acronym = 'OSO' AND sport_type = 'SOFTBALL' LIMIT 1;
  SELECT id INTO v_agu FROM teams WHERE acronym = 'AGU' AND sport_type = 'SOFTBALL' LIMIT 1;

  -- ── Partido ───────────────────────────────────────────────
  SELECT m.id INTO v_match
    FROM match m
    JOIN teams ht ON ht.id = m.home_team_id
    JOIN teams at ON at.id = m.away_team_id
   WHERE ht.acronym = 'OSO' AND at.acronym = 'AGU'
     AND m.sport_type = 'SOFTBALL'
   LIMIT 1;

  RAISE NOTICE 'IDs → OSO: %, AGU: %, Partido: %', v_oso, v_agu, v_match;

  -- ── Jugadoras OSAS DEL NORTE (OSO) ────────────────────────
  INSERT INTO players (full_name, jersey_name, jersey_number, birth_date)
  VALUES ('Carolina Restrepo',  'Restrepo',  3,  '1998-03-12') RETURNING id INTO p_oso1;

  INSERT INTO players (full_name, jersey_name, jersey_number, birth_date)
  VALUES ('Valentina Torres',   'Torres',    7,  '2000-06-25') RETURNING id INTO p_oso2;

  INSERT INTO players (full_name, jersey_name, jersey_number, birth_date)
  VALUES ('Mariana Ospina',     'Ospina',    11, '1997-11-04') RETURNING id INTO p_oso3;

  INSERT INTO players (full_name, jersey_name, jersey_number, birth_date)
  VALUES ('Daniela Ríos',       'Ríos',      15, '2001-02-18') RETURNING id INTO p_oso4;

  INSERT INTO players (full_name, jersey_name, jersey_number, birth_date)
  VALUES ('Alejandra Cano',     'Cano',      21, '1999-08-30') RETURNING id INTO p_oso5;

  INSERT INTO players (full_name, jersey_name, jersey_number, birth_date)
  VALUES ('Juliana Morales',    'Morales',   24, '2002-05-07') RETURNING id INTO p_oso6;

  INSERT INTO players (full_name, jersey_name, jersey_number, birth_date)
  VALUES ('Natalia Gómez',      'Gómez',     28, '1996-09-14') RETURNING id INTO p_oso7;

  INSERT INTO players (full_name, jersey_name, jersey_number, birth_date)
  VALUES ('Sofía Herrera',      'Herrera',   33, '2003-01-22') RETURNING id INTO p_oso8;

  INSERT INTO players (full_name, jersey_name, jersey_number, birth_date)
  VALUES ('Isabella Vargas',    'Vargas',    45, '1995-12-03') RETURNING id INTO p_oso9;

  -- Asignar a equipo
  UPDATE players SET team_id = v_oso
   WHERE id IN (p_oso1, p_oso2, p_oso3, p_oso4, p_oso5, p_oso6, p_oso7, p_oso8, p_oso9);

  -- ── Jugadoras ÁGUILAS DEL VALLE (AGU) ─────────────────────
  INSERT INTO players (full_name, jersey_name, jersey_number, birth_date)
  VALUES ('Camila Pedraza',     'Pedraza',   2,  '1999-04-17') RETURNING id INTO p_agu1;

  INSERT INTO players (full_name, jersey_name, jersey_number, birth_date)
  VALUES ('Laura Montoya',      'Montoya',   5,  '2001-07-09') RETURNING id INTO p_agu2;

  INSERT INTO players (full_name, jersey_name, jersey_number, birth_date)
  VALUES ('Andrea Jiménez',     'Jiménez',   8,  '1998-10-23') RETURNING id INTO p_agu3;

  INSERT INTO players (full_name, jersey_name, jersey_number, birth_date)
  VALUES ('Paola Castillo',     'Castillo',  12, '2000-01-31') RETURNING id INTO p_agu4;

  INSERT INTO players (full_name, jersey_name, jersey_number, birth_date)
  VALUES ('Verónica Salcedo',   'Salcedo',   17, '1997-05-15') RETURNING id INTO p_agu5;

  INSERT INTO players (full_name, jersey_name, jersey_number, birth_date)
  VALUES ('Manuela Arango',     'Arango',    22, '2002-08-28') RETURNING id INTO p_agu6;

  INSERT INTO players (full_name, jersey_name, jersey_number, birth_date)
  VALUES ('Ximena Patiño',      'Patiño',    27, '1996-03-06') RETURNING id INTO p_agu7;

  INSERT INTO players (full_name, jersey_name, jersey_number, birth_date)
  VALUES ('Luisa Fernanda Gil', 'Gil',       31, '2003-11-19') RETURNING id INTO p_agu8;

  INSERT INTO players (full_name, jersey_name, jersey_number, birth_date)
  VALUES ('Diana Mejía',        'Mejía',     44, '1994-06-08') RETURNING id INTO p_agu9;

  -- Asignar a equipo
  UPDATE players SET team_id = v_agu
   WHERE id IN (p_agu1, p_agu2, p_agu3, p_agu4, p_agu5, p_agu6, p_agu7, p_agu8, p_agu9);

  -- ── player_match OSO — home team ──────────────────────────
  -- Orden │ Jugadora    │ Posición
  --   1   │ Torres      │ SS  (abre el juego, buen contacto)
  --   2   │ Restrepo    │ 2B
  --   3   │ Ospina      │ LF  (bateadora de poder)
  --   4   │ Vargas      │ 1B  (cleanup)
  --   5   │ Cano        │ 3B
  --   6   │ Morales     │ CF
  --   7   │ Ríos        │ RF
  --   8   │ Gómez       │ C
  --   9   │ Herrera     │ P   (lanzadora)

  INSERT INTO player_match (match_id, team_id, player_id, in_, batting_order, field_position)
  VALUES
    (v_match, v_oso, p_oso2, now(), 1, 'SS'),
    (v_match, v_oso, p_oso1, now(), 2, '2B'),
    (v_match, v_oso, p_oso3, now(), 3, 'LF'),
    (v_match, v_oso, p_oso9, now(), 4, '1B'),
    (v_match, v_oso, p_oso5, now(), 5, '3B'),
    (v_match, v_oso, p_oso6, now(), 6, 'CF'),
    (v_match, v_oso, p_oso4, now(), 7, 'RF'),
    (v_match, v_oso, p_oso7, now(), 8, 'C'),
    (v_match, v_oso, p_oso8, now(), 9, 'P');

  -- ── player_match AGU — away team ──────────────────────────
  -- Orden │ Jugadora    │ Posición
  --   1   │ Pedraza     │ CF
  --   2   │ Jiménez     │ SS
  --   3   │ Montoya     │ 3B  (power hitter)
  --   4   │ Salcedo     │ 1B  (cleanup)
  --   5   │ Castillo    │ LF
  --   6   │ Arango      │ 2B
  --   7   │ Mejía       │ RF
  --   8   │ Gil         │ C
  --   9   │ Patiño      │ P   (lanzadora)

  INSERT INTO player_match (match_id, team_id, player_id, in_, batting_order, field_position)
  VALUES
    (v_match, v_agu, p_agu1, now(), 1, 'CF'),
    (v_match, v_agu, p_agu3, now(), 2, 'SS'),
    (v_match, v_agu, p_agu2, now(), 3, '3B'),
    (v_match, v_agu, p_agu5, now(), 4, '1B'),
    (v_match, v_agu, p_agu4, now(), 5, 'LF'),
    (v_match, v_agu, p_agu6, now(), 6, '2B'),
    (v_match, v_agu, p_agu9, now(), 7, 'RF'),
    (v_match, v_agu, p_agu8, now(), 8, 'C'),
    (v_match, v_agu, p_agu7, now(), 9, 'P');

  RAISE NOTICE 'Seed completado: 18 jugadoras insertadas en el partido %', v_match;
END $$;
