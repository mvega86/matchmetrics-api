-- ============================================================
-- SEED: Torneo de softball de prueba con 4 equipos y 6 partidos
-- Ejecutar manualmente en la BD de desarrollo
-- ============================================================

-- 1. Equipos de softball
INSERT INTO teams (name, acronym, stadium, sport_type)
VALUES
  ('Tigres Softball Club',   'TIG', 'Estadio El Campín',         'SOFTBALL'),
  ('Leonas de Bogotá',       'LEO', 'Estadio El Salitre',        'SOFTBALL'),
  ('Osas del Norte',         'OSO', 'Parque Deportivo Municipal', 'SOFTBALL'),
  ('Águilas del Valle',      'AGU', 'Estadio Pascual Guerrero',   'SOFTBALL')
ON CONFLICT DO NOTHING;

-- 2. Torneo
INSERT INTO tournament (name, description, sport_type, status, start_date, end_date,
                         organizer, location, country, category,
                         created_at, updated_at)
VALUES (
  'Copa Softball Femenina 2026',
  'Torneo de prueba para validar la vista de partidos',
  'SOFTBALL',
  'IN_PROGRESS',
  CURRENT_DATE,
  CURRENT_DATE + INTERVAL '6 days',
  'Liga Nacional de Softball',
  'Bogotá, Colombia',
  'Colombia',
  'Open Femenino',
  now(), now()
)
ON CONFLICT DO NOTHING;

-- 3. Variables de apoyo (usamos CTEs para el resto)
DO $$
DECLARE
  v_tig  BIGINT;
  v_leo  BIGINT;
  v_oso  BIGINT;
  v_agu  BIGINT;
  v_tour BIGINT;
BEGIN
  SELECT id INTO v_tig FROM teams WHERE acronym = 'TIG' AND sport_type = 'SOFTBALL' LIMIT 1;
  SELECT id INTO v_leo FROM teams WHERE acronym = 'LEO' AND sport_type = 'SOFTBALL' LIMIT 1;
  SELECT id INTO v_oso FROM teams WHERE acronym = 'OSO' AND sport_type = 'SOFTBALL' LIMIT 1;
  SELECT id INTO v_agu FROM teams WHERE acronym = 'AGU' AND sport_type = 'SOFTBALL' LIMIT 1;
  SELECT id INTO v_tour FROM tournament WHERE name = 'Copa Softball Femenina 2026' LIMIT 1;

  -- 4. Asociar equipos al torneo
  INSERT INTO tournament_team (tournament_id, team_id)
  VALUES
    (v_tour, v_tig),
    (v_tour, v_leo),
    (v_tour, v_oso),
    (v_tour, v_agu)
  ON CONFLICT DO NOTHING;

  -- 5. Generar 6 partidos liguilla (round-robin): TIG-LEO, TIG-OSO, TIG-AGU, LEO-OSO, LEO-AGU, OSO-AGU
  INSERT INTO match (tournament_id, sport_type, state, phase, location,
                     home_team_id, away_team_id, start_first_time, created_at, updated_at)
  VALUES
    (v_tour, 'SOFTBALL', 'FINISHED', 'NOT_STARTED',
     (SELECT stadium FROM teams WHERE id = v_tig),
     v_tig, v_leo, CURRENT_DATE - INTERVAL '2 days' + TIME '10:00', now(), now()),

    (v_tour, 'SOFTBALL', 'FINISHED', 'NOT_STARTED',
     (SELECT stadium FROM teams WHERE id = v_leo),
     v_leo, v_oso, CURRENT_DATE - INTERVAL '1 day'  + TIME '10:00', now(), now()),

    (v_tour, 'SOFTBALL', 'STARTED',  'NOT_STARTED',
     (SELECT stadium FROM teams WHERE id = v_oso),
     v_oso, v_agu, CURRENT_DATE + TIME '10:00', now(), now()),

    (v_tour, 'SOFTBALL', 'PENDING',  'NOT_STARTED',
     (SELECT stadium FROM teams WHERE id = v_tig),
     v_tig, v_agu, CURRENT_DATE + INTERVAL '1 day'  + TIME '10:00', now(), now()),

    (v_tour, 'SOFTBALL', 'PENDING',  'NOT_STARTED',
     (SELECT stadium FROM teams WHERE id = v_leo),
     v_leo, v_agu, CURRENT_DATE + INTERVAL '2 days' + TIME '10:00', now(), now()),

    (v_tour, 'SOFTBALL', 'PENDING',  'NOT_STARTED',
     (SELECT stadium FROM teams WHERE id = v_tig),
     v_tig, v_oso, CURRENT_DATE + INTERVAL '3 days' + TIME '10:00', now(), now());

  RAISE NOTICE 'Seed completado: torneo_id=%, equipos: TIG=%, LEO=%, OSO=%, AGU=%',
    v_tour, v_tig, v_leo, v_oso, v_agu;
END $$;
