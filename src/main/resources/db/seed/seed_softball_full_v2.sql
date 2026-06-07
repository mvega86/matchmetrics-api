-- ============================================================
-- SEED v2: Gestión completa de partidos softball 2026
-- Acciones:
--   1. Cierra partidos PENDING cuya fecha ya pasó
--   2. Completa batting orders/posiciones donde faltan
--   3. Crea torneo "Jornada 2" con 6 partidos + 3 play-offs
--   4. Inserta lineups completos para todos los equipos
--
-- Requiere: equipos TIG/LEO/OSO/AGU y sus jugadoras ya creados
--           (seed_softball_players_oso_agu + seed_softball_tigres_leonas)
-- Ejecutar manualmente en BD de desarrollo
-- ============================================================

-- ── 1. Diagnóstico: partidos actuales ────────────────────────
-- (descomenta para ver el estado antes de ejecutar)
-- SELECT m.id, ht.acronym||' vs '||at.acronym AS partido,
--        m.state, m.start_first_time::date
--   FROM match m
--   JOIN teams ht ON ht.id = m.home_team_id
--   JOIN teams at ON at.id = m.away_team_id
--  WHERE m.sport_type = 'SOFTBALL'
--  ORDER BY m.start_first_time;

-- ── 2. Cerrar partidos PENDING cuya fecha ya pasó ────────────
UPDATE match
   SET state = 'FINISHED'
 WHERE sport_type = 'SOFTBALL'
   AND state = 'PENDING'
   AND start_first_time < NOW() - INTERVAL '2 hours';

-- ── 3. Completar lineups con batting order donde faltan ───────
DO $$
DECLARE
  v_tig   BIGINT;
  v_leo   BIGINT;
  v_oso   BIGINT;
  v_agu   BIGINT;
  v_tour2 BIGINT;

  -- IDs de los nuevos partidos (Jornada 2)
  v_j2_m1 BIGINT;  v_j2_m2 BIGINT;  v_j2_m3 BIGINT;
  v_j2_m4 BIGINT;  v_j2_m5 BIGINT;  v_j2_m6 BIGINT;
  v_sf1   BIGINT;  v_sf2   BIGINT;  v_fin   BIGINT;

BEGIN
  -- ── Equipos ──────────────────────────────────────────────
  SELECT id INTO v_tig FROM teams WHERE acronym = 'TIG' AND sport_type = 'SOFTBALL' LIMIT 1;
  SELECT id INTO v_leo FROM teams WHERE acronym = 'LEO' AND sport_type = 'SOFTBALL' LIMIT 1;
  SELECT id INTO v_oso FROM teams WHERE acronym = 'OSO' AND sport_type = 'SOFTBALL' LIMIT 1;
  SELECT id INTO v_agu FROM teams WHERE acronym = 'AGU' AND sport_type = 'SOFTBALL' LIMIT 1;

  RAISE NOTICE 'Equipos → TIG:%, LEO:%, OSO:%, AGU:%', v_tig, v_leo, v_oso, v_agu;

  -- ── 3a. Completar batting order de AGU en partidos PENDING ─
  -- Para cada match donde AGU tiene player_match sin batting_order,
  -- asignamos orden 1-9 según jersey_number
  UPDATE player_match pm
     SET batting_order  = sub.rn,
         in_            = COALESCE(pm.in_, now()),
         field_position = CASE sub.rn
                            WHEN 1 THEN 'CF' WHEN 2 THEN 'SS'
                            WHEN 3 THEN '3B' WHEN 4 THEN '1B'
                            WHEN 5 THEN 'LF' WHEN 6 THEN '2B'
                            WHEN 7 THEN 'RF' WHEN 8 THEN 'C'
                            WHEN 9 THEN 'P'  ELSE NULL
                          END
    FROM (
      SELECT p.id AS pid,
             ROW_NUMBER() OVER (ORDER BY p.jersey_number) AS rn
        FROM players p
       WHERE p.team_id = v_agu
       ORDER BY p.jersey_number
       LIMIT 9
    ) sub
   WHERE pm.player_id = sub.pid
     AND pm.match_id IN (
       SELECT m.id FROM match m
        WHERE m.sport_type = 'SOFTBALL'
          AND m.state = 'PENDING'
          AND (m.home_team_id = v_agu OR m.away_team_id = v_agu)
     )
     AND pm.batting_order IS NULL;

  -- ── 3b. Completar batting order de OSO en partidos PENDING ─
  UPDATE player_match pm
     SET batting_order  = sub.rn,
         in_            = COALESCE(pm.in_, now()),
         field_position = CASE sub.rn
                            WHEN 1 THEN 'SS' WHEN 2 THEN '2B'
                            WHEN 3 THEN 'LF' WHEN 4 THEN '1B'
                            WHEN 5 THEN '3B' WHEN 6 THEN 'CF'
                            WHEN 7 THEN 'RF' WHEN 8 THEN 'C'
                            WHEN 9 THEN 'P'  ELSE NULL
                          END
    FROM (
      SELECT p.id AS pid,
             ROW_NUMBER() OVER (ORDER BY p.jersey_number) AS rn
        FROM players p
       WHERE p.team_id = v_oso
       ORDER BY p.jersey_number
       LIMIT 9
    ) sub
   WHERE pm.player_id = sub.pid
     AND pm.match_id IN (
       SELECT m.id FROM match m
        WHERE m.sport_type = 'SOFTBALL'
          AND m.state = 'PENDING'
          AND (m.home_team_id = v_oso OR m.away_team_id = v_oso)
     )
     AND pm.batting_order IS NULL;

  -- ── 3c. Asegurar que los bench-players tengan in_ = NULL ───
  UPDATE player_match pm
     SET batting_order = NULL,
         in_           = NULL
    FROM (
      SELECT p.id AS pid
        FROM players p
       WHERE p.team_id IN (v_agu, v_oso)
       ORDER BY p.jersey_number
      OFFSET 9
    ) sub
   WHERE pm.player_id = sub.pid
     AND pm.match_id IN (
       SELECT m.id FROM match m WHERE m.sport_type = 'SOFTBALL'
     )
     AND pm.batting_order IS NULL;

  -- ── 4. Crear Torneo "Jornada 2" ───────────────────────────
  INSERT INTO tournament (name, description, sport_type, status,
                          start_date, end_date, organizer, location,
                          country, category, created_at, updated_at)
  VALUES ('Copa Softball Femenina 2026 - Jornada 2',
          'Segunda vuelta de la liga + play-offs',
          'SOFTBALL', 'UPCOMING',
          '2026-06-14', '2026-07-12',
          'Liga Nacional de Softball', 'Colombia',
          'Colombia', 'Open Femenino',
          now(), now())
  RETURNING id INTO v_tour2;

  -- Asociar equipos al torneo
  INSERT INTO tournament_team (tournament_id, team_id)
  VALUES (v_tour2, v_tig), (v_tour2, v_leo),
         (v_tour2, v_oso), (v_tour2, v_agu)
  ON CONFLICT DO NOTHING;

  RAISE NOTICE 'Torneo Jornada 2 creado: id=%', v_tour2;

  -- ── 5. Crear 9 partidos ───────────────────────────────────
  -- Jornada 2 (segunda vuelta): 6 partidos + 2 semifinales + 1 final

  INSERT INTO match (tournament_id, sport_type, state, phase, location,
                     home_team_id, away_team_id, start_first_time,
                     created_at, updated_at)
  VALUES (v_tour2, 'SOFTBALL', 'PENDING', 'NOT_STARTED',
          (SELECT stadium FROM teams WHERE id = v_leo),
          v_leo, v_tig, '2026-06-14 10:00:00', now(), now())
  RETURNING id INTO v_j2_m1;

  INSERT INTO match (tournament_id, sport_type, state, phase, location,
                     home_team_id, away_team_id, start_first_time,
                     created_at, updated_at)
  VALUES (v_tour2, 'SOFTBALL', 'PENDING', 'NOT_STARTED',
          (SELECT stadium FROM teams WHERE id = v_oso),
          v_oso, v_leo, '2026-06-14 14:00:00', now(), now())
  RETURNING id INTO v_j2_m2;

  INSERT INTO match (tournament_id, sport_type, state, phase, location,
                     home_team_id, away_team_id, start_first_time,
                     created_at, updated_at)
  VALUES (v_tour2, 'SOFTBALL', 'PENDING', 'NOT_STARTED',
          (SELECT stadium FROM teams WHERE id = v_agu),
          v_agu, v_oso, '2026-06-21 10:00:00', now(), now())
  RETURNING id INTO v_j2_m3;

  INSERT INTO match (tournament_id, sport_type, state, phase, location,
                     home_team_id, away_team_id, start_first_time,
                     created_at, updated_at)
  VALUES (v_tour2, 'SOFTBALL', 'PENDING', 'NOT_STARTED',
          (SELECT stadium FROM teams WHERE id = v_agu),
          v_agu, v_tig, '2026-06-21 14:00:00', now(), now())
  RETURNING id INTO v_j2_m4;

  INSERT INTO match (tournament_id, sport_type, state, phase, location,
                     home_team_id, away_team_id, start_first_time,
                     created_at, updated_at)
  VALUES (v_tour2, 'SOFTBALL', 'PENDING', 'NOT_STARTED',
          (SELECT stadium FROM teams WHERE id = v_leo),
          v_leo, v_agu, '2026-06-28 10:00:00', now(), now())
  RETURNING id INTO v_j2_m5;

  INSERT INTO match (tournament_id, sport_type, state, phase, location,
                     home_team_id, away_team_id, start_first_time,
                     created_at, updated_at)
  VALUES (v_tour2, 'SOFTBALL', 'PENDING', 'NOT_STARTED',
          (SELECT stadium FROM teams WHERE id = v_tig),
          v_tig, v_oso, '2026-06-28 14:00:00', now(), now())
  RETURNING id INTO v_j2_m6;

  -- Semifinales (sin lineups predefinidos → probar editor desde cero)
  INSERT INTO match (tournament_id, sport_type, state, phase, location,
                     home_team_id, away_team_id, start_first_time,
                     created_at, updated_at)
  VALUES (v_tour2, 'SOFTBALL', 'PENDING', 'NOT_STARTED',
          (SELECT stadium FROM teams WHERE id = v_tig),
          v_tig, v_agu, '2026-07-05 10:00:00', now(), now())
  RETURNING id INTO v_sf1;

  INSERT INTO match (tournament_id, sport_type, state, phase, location,
                     home_team_id, away_team_id, start_first_time,
                     created_at, updated_at)
  VALUES (v_tour2, 'SOFTBALL', 'PENDING', 'NOT_STARTED',
          (SELECT stadium FROM teams WHERE id = v_leo),
          v_leo, v_oso, '2026-07-05 14:00:00', now(), now())
  RETURNING id INTO v_sf2;

  -- Final
  INSERT INTO match (tournament_id, sport_type, state, phase, location,
                     home_team_id, away_team_id, start_first_time,
                     created_at, updated_at)
  VALUES (v_tour2, 'SOFTBALL', 'PENDING', 'NOT_STARTED',
          'Estadio Nacional Softball', v_tig, v_leo,
          '2026-07-12 15:00:00', now(), now())
  RETURNING id INTO v_fin;

  RAISE NOTICE 'Partidos creados: J2=[%, %, %, %, %, %], SF=[%, %], Final=%',
    v_j2_m1, v_j2_m2, v_j2_m3, v_j2_m4, v_j2_m5, v_j2_m6, v_sf1, v_sf2, v_fin;

  -- ── 6. Función interna: insertar lineup completo ──────────
  -- Para cada partido de Jornada 2 (con lineups precargados),
  -- insertamos 9 titulares + suplentes por equipo.
  -- La posición sigue el patrón de jersey_number ASC.

  -- ─ Helper macro (repetido por match+team) ─────────────────
  -- Titulares TIG en J2-M1 (LEO local, TIG visitante)
  INSERT INTO player_match (match_id, player_id, in_, batting_order, field_position)
  SELECT v_j2_m1, p.id, now(), sub.rn,
         CASE sub.rn WHEN 1 THEN 'SS' WHEN 2 THEN '2B' WHEN 3 THEN 'LF'
                     WHEN 4 THEN '1B' WHEN 5 THEN '3B' WHEN 6 THEN 'CF'
                     WHEN 7 THEN 'RF' WHEN 8 THEN 'C'  WHEN 9 THEN 'P' END
    FROM players p
    JOIN (SELECT id, ROW_NUMBER() OVER (ORDER BY jersey_number) AS rn
            FROM players WHERE team_id = v_tig
           ORDER BY jersey_number LIMIT 9) sub ON sub.id = p.id;

  INSERT INTO player_match (match_id, player_id, in_, batting_order, field_position)
  SELECT v_j2_m1, id, NULL, NULL, NULL
    FROM players WHERE team_id = v_tig ORDER BY jersey_number OFFSET 9;

  -- Titulares LEO en J2-M1
  INSERT INTO player_match (match_id, player_id, in_, batting_order, field_position)
  SELECT v_j2_m1, p.id, now(), sub.rn,
         CASE sub.rn WHEN 1 THEN 'CF' WHEN 2 THEN 'SS' WHEN 3 THEN '3B'
                     WHEN 4 THEN '1B' WHEN 5 THEN 'LF' WHEN 6 THEN '2B'
                     WHEN 7 THEN 'RF' WHEN 8 THEN 'C'  WHEN 9 THEN 'P' END
    FROM players p
    JOIN (SELECT id, ROW_NUMBER() OVER (ORDER BY jersey_number) AS rn
            FROM players WHERE team_id = v_leo
           ORDER BY jersey_number LIMIT 9) sub ON sub.id = p.id;

  INSERT INTO player_match (match_id, player_id, in_, batting_order, field_position)
  SELECT v_j2_m1, id, NULL, NULL, NULL
    FROM players WHERE team_id = v_leo ORDER BY jersey_number OFFSET 9;

  -- ─ J2-M2: OSO vs LEO ──────────────────────────────────────
  INSERT INTO player_match (match_id, player_id, in_, batting_order, field_position)
  SELECT v_j2_m2, p.id, now(), sub.rn,
         CASE sub.rn WHEN 1 THEN 'SS' WHEN 2 THEN '2B' WHEN 3 THEN 'LF'
                     WHEN 4 THEN '1B' WHEN 5 THEN '3B' WHEN 6 THEN 'CF'
                     WHEN 7 THEN 'RF' WHEN 8 THEN 'C'  WHEN 9 THEN 'P' END
    FROM players p
    JOIN (SELECT id, ROW_NUMBER() OVER (ORDER BY jersey_number) AS rn
            FROM players WHERE team_id = v_oso
           ORDER BY jersey_number LIMIT 9) sub ON sub.id = p.id;

  INSERT INTO player_match (match_id, player_id, in_, batting_order, field_position)
  SELECT v_j2_m2, id, NULL, NULL, NULL
    FROM players WHERE team_id = v_oso ORDER BY jersey_number OFFSET 9;

  INSERT INTO player_match (match_id, player_id, in_, batting_order, field_position)
  SELECT v_j2_m2, p.id, now(), sub.rn,
         CASE sub.rn WHEN 1 THEN 'CF' WHEN 2 THEN 'SS' WHEN 3 THEN '3B'
                     WHEN 4 THEN '1B' WHEN 5 THEN 'LF' WHEN 6 THEN '2B'
                     WHEN 7 THEN 'RF' WHEN 8 THEN 'C'  WHEN 9 THEN 'P' END
    FROM players p
    JOIN (SELECT id, ROW_NUMBER() OVER (ORDER BY jersey_number) AS rn
            FROM players WHERE team_id = v_leo
           ORDER BY jersey_number LIMIT 9) sub ON sub.id = p.id;

  INSERT INTO player_match (match_id, player_id, in_, batting_order, field_position)
  SELECT v_j2_m2, id, NULL, NULL, NULL
    FROM players WHERE team_id = v_leo ORDER BY jersey_number OFFSET 9;

  -- ─ J2-M3: AGU vs OSO ──────────────────────────────────────
  INSERT INTO player_match (match_id, player_id, in_, batting_order, field_position)
  SELECT v_j2_m3, p.id, now(), sub.rn,
         CASE sub.rn WHEN 1 THEN 'CF' WHEN 2 THEN 'SS' WHEN 3 THEN '3B'
                     WHEN 4 THEN '1B' WHEN 5 THEN 'LF' WHEN 6 THEN '2B'
                     WHEN 7 THEN 'RF' WHEN 8 THEN 'C'  WHEN 9 THEN 'P' END
    FROM players p
    JOIN (SELECT id, ROW_NUMBER() OVER (ORDER BY jersey_number) AS rn
            FROM players WHERE team_id = v_agu
           ORDER BY jersey_number LIMIT 9) sub ON sub.id = p.id;

  INSERT INTO player_match (match_id, player_id, in_, batting_order, field_position)
  SELECT v_j2_m3, id, NULL, NULL, NULL
    FROM players WHERE team_id = v_agu ORDER BY jersey_number OFFSET 9;

  INSERT INTO player_match (match_id, player_id, in_, batting_order, field_position)
  SELECT v_j2_m3, p.id, now(), sub.rn,
         CASE sub.rn WHEN 1 THEN 'SS' WHEN 2 THEN '2B' WHEN 3 THEN 'LF'
                     WHEN 4 THEN '1B' WHEN 5 THEN '3B' WHEN 6 THEN 'CF'
                     WHEN 7 THEN 'RF' WHEN 8 THEN 'C'  WHEN 9 THEN 'P' END
    FROM players p
    JOIN (SELECT id, ROW_NUMBER() OVER (ORDER BY jersey_number) AS rn
            FROM players WHERE team_id = v_oso
           ORDER BY jersey_number LIMIT 9) sub ON sub.id = p.id;

  INSERT INTO player_match (match_id, player_id, in_, batting_order, field_position)
  SELECT v_j2_m3, id, NULL, NULL, NULL
    FROM players WHERE team_id = v_oso ORDER BY jersey_number OFFSET 9;

  -- ─ J2-M4: AGU vs TIG ──────────────────────────────────────
  INSERT INTO player_match (match_id, player_id, in_, batting_order, field_position)
  SELECT v_j2_m4, p.id, now(), sub.rn,
         CASE sub.rn WHEN 1 THEN 'CF' WHEN 2 THEN 'SS' WHEN 3 THEN '3B'
                     WHEN 4 THEN '1B' WHEN 5 THEN 'LF' WHEN 6 THEN '2B'
                     WHEN 7 THEN 'RF' WHEN 8 THEN 'C'  WHEN 9 THEN 'P' END
    FROM players p
    JOIN (SELECT id, ROW_NUMBER() OVER (ORDER BY jersey_number) AS rn
            FROM players WHERE team_id = v_agu
           ORDER BY jersey_number LIMIT 9) sub ON sub.id = p.id;

  INSERT INTO player_match (match_id, player_id, in_, batting_order, field_position)
  SELECT v_j2_m4, id, NULL, NULL, NULL
    FROM players WHERE team_id = v_agu ORDER BY jersey_number OFFSET 9;

  INSERT INTO player_match (match_id, player_id, in_, batting_order, field_position)
  SELECT v_j2_m4, p.id, now(), sub.rn,
         CASE sub.rn WHEN 1 THEN 'SS' WHEN 2 THEN '2B' WHEN 3 THEN 'LF'
                     WHEN 4 THEN '1B' WHEN 5 THEN '3B' WHEN 6 THEN 'CF'
                     WHEN 7 THEN 'RF' WHEN 8 THEN 'C'  WHEN 9 THEN 'P' END
    FROM players p
    JOIN (SELECT id, ROW_NUMBER() OVER (ORDER BY jersey_number) AS rn
            FROM players WHERE team_id = v_tig
           ORDER BY jersey_number LIMIT 9) sub ON sub.id = p.id;

  INSERT INTO player_match (match_id, player_id, in_, batting_order, field_position)
  SELECT v_j2_m4, id, NULL, NULL, NULL
    FROM players WHERE team_id = v_tig ORDER BY jersey_number OFFSET 9;

  -- ─ J2-M5: LEO vs AGU ──────────────────────────────────────
  INSERT INTO player_match (match_id, player_id, in_, batting_order, field_position)
  SELECT v_j2_m5, p.id, now(), sub.rn,
         CASE sub.rn WHEN 1 THEN 'CF' WHEN 2 THEN 'SS' WHEN 3 THEN '3B'
                     WHEN 4 THEN '1B' WHEN 5 THEN 'LF' WHEN 6 THEN '2B'
                     WHEN 7 THEN 'RF' WHEN 8 THEN 'C'  WHEN 9 THEN 'P' END
    FROM players p
    JOIN (SELECT id, ROW_NUMBER() OVER (ORDER BY jersey_number) AS rn
            FROM players WHERE team_id = v_leo
           ORDER BY jersey_number LIMIT 9) sub ON sub.id = p.id;

  INSERT INTO player_match (match_id, player_id, in_, batting_order, field_position)
  SELECT v_j2_m5, id, NULL, NULL, NULL
    FROM players WHERE team_id = v_leo ORDER BY jersey_number OFFSET 9;

  INSERT INTO player_match (match_id, player_id, in_, batting_order, field_position)
  SELECT v_j2_m5, p.id, now(), sub.rn,
         CASE sub.rn WHEN 1 THEN 'CF' WHEN 2 THEN 'SS' WHEN 3 THEN '3B'
                     WHEN 4 THEN '1B' WHEN 5 THEN 'LF' WHEN 6 THEN '2B'
                     WHEN 7 THEN 'RF' WHEN 8 THEN 'C'  WHEN 9 THEN 'P' END
    FROM players p
    JOIN (SELECT id, ROW_NUMBER() OVER (ORDER BY jersey_number) AS rn
            FROM players WHERE team_id = v_agu
           ORDER BY jersey_number LIMIT 9) sub ON sub.id = p.id;

  INSERT INTO player_match (match_id, player_id, in_, batting_order, field_position)
  SELECT v_j2_m5, id, NULL, NULL, NULL
    FROM players WHERE team_id = v_agu ORDER BY jersey_number OFFSET 9;

  -- ─ J2-M6: TIG vs OSO ──────────────────────────────────────
  INSERT INTO player_match (match_id, player_id, in_, batting_order, field_position)
  SELECT v_j2_m6, p.id, now(), sub.rn,
         CASE sub.rn WHEN 1 THEN 'SS' WHEN 2 THEN '2B' WHEN 3 THEN 'LF'
                     WHEN 4 THEN '1B' WHEN 5 THEN '3B' WHEN 6 THEN 'CF'
                     WHEN 7 THEN 'RF' WHEN 8 THEN 'C'  WHEN 9 THEN 'P' END
    FROM players p
    JOIN (SELECT id, ROW_NUMBER() OVER (ORDER BY jersey_number) AS rn
            FROM players WHERE team_id = v_tig
           ORDER BY jersey_number LIMIT 9) sub ON sub.id = p.id;

  INSERT INTO player_match (match_id, player_id, in_, batting_order, field_position)
  SELECT v_j2_m6, id, NULL, NULL, NULL
    FROM players WHERE team_id = v_tig ORDER BY jersey_number OFFSET 9;

  INSERT INTO player_match (match_id, player_id, in_, batting_order, field_position)
  SELECT v_j2_m6, p.id, now(), sub.rn,
         CASE sub.rn WHEN 1 THEN 'SS' WHEN 2 THEN '2B' WHEN 3 THEN 'LF'
                     WHEN 4 THEN '1B' WHEN 5 THEN '3B' WHEN 6 THEN 'CF'
                     WHEN 7 THEN 'RF' WHEN 8 THEN 'C'  WHEN 9 THEN 'P' END
    FROM players p
    JOIN (SELECT id, ROW_NUMBER() OVER (ORDER BY jersey_number) AS rn
            FROM players WHERE team_id = v_oso
           ORDER BY jersey_number LIMIT 9) sub ON sub.id = p.id;

  INSERT INTO player_match (match_id, player_id, in_, batting_order, field_position)
  SELECT v_j2_m6, id, NULL, NULL, NULL
    FROM players WHERE team_id = v_oso ORDER BY jersey_number OFFSET 9;

  -- ─ SEMIFINALES: sin lineups (probar editor desde cero) ────
  -- Solo registrar los jugadores como disponibles (in_ = NULL)
  INSERT INTO player_match (match_id, player_id, in_, batting_order, field_position)
  SELECT v_sf1, id, NULL, NULL, NULL FROM players WHERE team_id IN (v_tig, v_agu);

  INSERT INTO player_match (match_id, player_id, in_, batting_order, field_position)
  SELECT v_sf2, id, NULL, NULL, NULL FROM players WHERE team_id IN (v_leo, v_oso);

  -- ─ FINAL: sin lineups ─────────────────────────────────────
  INSERT INTO player_match (match_id, player_id, in_, batting_order, field_position)
  SELECT v_fin, id, NULL, NULL, NULL FROM players WHERE team_id IN (v_tig, v_leo);

  RAISE NOTICE 'Seed v2 completado:';
  RAISE NOTICE '  - Partidos pasados cerrados';
  RAISE NOTICE '  - Batting orders AGU/OSO completados en partidos pendientes';
  RAISE NOTICE '  - Jornada 2: 6 partidos con lineups + 2 semis + final (sin lineups)';
END $$;

-- ── Verificación final ────────────────────────────────────────
SELECT
  m.id,
  ht.acronym || ' vs ' || at.acronym AS partido,
  m.state,
  to_char(m.start_first_time, 'YYYY-MM-DD HH24:MI') AS fecha,
  (SELECT count(*) FROM player_match pm
    WHERE pm.match_id = m.id AND pm.in_ IS NOT NULL) AS titulares,
  (SELECT count(*) FROM player_match pm
    WHERE pm.match_id = m.id AND pm.batting_order IS NOT NULL) AS con_orden
FROM match m
JOIN teams ht ON ht.id = m.home_team_id
JOIN teams at ON at.id = m.away_team_id
WHERE m.sport_type = 'SOFTBALL'
ORDER BY m.start_first_time;
