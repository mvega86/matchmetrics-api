-- ============================================================
-- V40__seed_softball_malaga.sql
-- Seed de producción: Liga Softball Málaga
-- Fuente: AVG300 rosters PDF (junio 2026)
-- Flyway garantiza ejecución única; ON CONFLICT / WHERE NOT EXISTS
-- añaden protección extra por si se reutiliza el script.
-- ============================================================

-- ── 1. Ajustes de esquema ─────────────────────────────────────
-- birth_date: no importamos DOB del roster → permitir NULL
ALTER TABLE players ALTER COLUMN birth_date DROP NOT NULL;

-- jersey_number: el dorsal 0 es válido en softball
-- Buscamos y eliminamos el CHECK (> 0) por nombre auto-generado
DO $$
DECLARE v_conname text;
BEGIN
    SELECT conname INTO v_conname
    FROM pg_constraint
    WHERE conrelid = 'players'::regclass
      AND contype = 'c'
      AND pg_get_constraintdef(oid) LIKE '%jersey_number%0%';
    IF v_conname IS NOT NULL THEN
        EXECUTE 'ALTER TABLE players DROP CONSTRAINT ' || quote_ident(v_conname);
    END IF;
END;
$$;
ALTER TABLE players ADD CONSTRAINT players_jersey_number_nonneg
    CHECK (jersey_number >= 0);

-- ── 2. Equipos ───────────────────────────────────────────────
INSERT INTO teams (name, acronym, sport_type)
SELECT v.name, v.acronym, 'SOFTBALL'
FROM (VALUES
    ('AGUILAS DE MIJAS',      'ADM'),
    ('ASTROS DE MARBELLA',    'ASM'),
    ('ESTRELLA LATINA',       'ELA'),
    ('LOS ANGELES DE MALAGA', 'LAM'),
    ('MARBELLA',              'MBL'),
    ('MARLINS DE MARBELLA',   'MRM'),
    ('RANGERS DE TEXAS',      'RDT'),
    ('TIGRES DE MARBELLA',    'TDM'),
    ('TOROS DEL VISO',        'TDV'),
    ('VENEMALAGA',            'VNM'),
    ('YANKEES DE MALAGA',     'YDM')
) AS v(name, acronym)
WHERE NOT EXISTS (
    SELECT 1 FROM teams t
    WHERE t.name = v.name AND t.sport_type = 'SOFTBALL'
);

-- ── 3. Jugadores con dorsal ───────────────────────────────────
-- ON CONFLICT (team_id, jersey_number) DO NOTHING protege ante
-- dorsales duplicados dentro del mismo equipo (ej. EL #50 x2,
-- Marbella #31 x2). El segundo INSERT se descarta silenciosamente.

INSERT INTO players (full_name, jersey_number, team_id)
SELECT v.full_name, v.jersey_number, t.id
FROM (VALUES
    -- ── AGUILAS DE MIJAS (20) ─────────────────────────────────
    ('AGUILAS DE MIJAS',      'Isam Issa AG',              1),
    ('AGUILAS DE MIJAS',      'Jose C Gomez C',            2),
    ('AGUILAS DE MIJAS',      'Jeffrey N Bohorquez R',     5),
    ('AGUILAS DE MIJAS',      'Rene M Boscan',             7),
    ('AGUILAS DE MIJAS',      'Edduym Roa AG',             10),
    ('AGUILAS DE MIJAS',      'Reny S Boscan',             12),
    ('AGUILAS DE MIJAS',      'Yonder Rodriguez AG',       13),
    ('AGUILAS DE MIJAS',      'Miguel Ameneiro AG',        14),
    ('AGUILAS DE MIJAS',      'Fernando Garcia AG',        15),
    ('AGUILAS DE MIJAS',      'Yojhan Barco AG',           18),
    ('AGUILAS DE MIJAS',      'Misael Vargas AG',          19),
    ('AGUILAS DE MIJAS',      'Jose G Avila',              22),
    ('AGUILAS DE MIJAS',      'Gabriel Araujo AG',         23),
    ('AGUILAS DE MIJAS',      'Hector Torrealba AG',       24),
    ('AGUILAS DE MIJAS',      'Jose M Romero',             26),
    ('AGUILAS DE MIJAS',      'Elio Chacon AG',            27),
    ('AGUILAS DE MIJAS',      'Mauricio Gonzalez AG',      28),
    ('AGUILAS DE MIJAS',      'German Rodriguez AG',       37),
    ('AGUILAS DE MIJAS',      'German Vargas AG',          38),
    ('AGUILAS DE MIJAS',      'Steven D Iriarte M',        50),
    -- ── ASTROS DE MARBELLA (13 visibles; 4 ocultos por corte de página) ──
    ('ASTROS DE MARBELLA',    'Adres Guevara AM',          0),
    ('ASTROS DE MARBELLA',    'Elieser C Sifones',         1),
    ('ASTROS DE MARBELLA',    'Cesar E Sanchez',           2),
    ('ASTROS DE MARBELLA',    'Alexis Lovera AM',          3),
    ('ASTROS DE MARBELLA',    'Javier E Diaz',             6),
    ('ASTROS DE MARBELLA',    'Junior J Del Valle',        8),
    ('ASTROS DE MARBELLA',    'Freddy A Guevara',          9),
    ('ASTROS DE MARBELLA',    'Carlos Perez Q',            11),
    ('ASTROS DE MARBELLA',    'Edison De La Ossa',         13),
    ('ASTROS DE MARBELLA',    'Luis R Yendis',             16),
    ('ASTROS DE MARBELLA',    'Josue S Mayorga',           21),
    ('ASTROS DE MARBELLA',    'Miguel A Morales',          23),
    ('ASTROS DE MARBELLA',    'Junior J Leal',             99),
    -- ── ESTRELLA LATINA (35 visibles; 1 oculto; #50 duplicado → segundo ignorado) ──
    ('ESTRELLA LATINA',       'Obdulio Gonzalez S',        1),
    ('ESTRELLA LATINA',       'Victor A Lora C',           2),
    ('ESTRELLA LATINA',       'Victor Cuello EL',          3),
    ('ESTRELLA LATINA',       'Jose A Sojo P',             4),
    ('ESTRELLA LATINA',       'Adam O Luna S',             5),
    ('ESTRELLA LATINA',       'Eric Rubio EL',             6),
    ('ESTRELLA LATINA',       'Ackynson Gonzalez G',       7),
    ('ESTRELLA LATINA',       'Rafael J Frias H',          8),
    ('ESTRELLA LATINA',       'Raul Betances S',           9),
    ('ESTRELLA LATINA',       'Digne de Leon S',           10),
    ('ESTRELLA LATINA',       'Juan R Herrera G',          13),
    ('ESTRELLA LATINA',       'Learsi I Castillo V',       16),
    ('ESTRELLA LATINA',       'Carlos Aguirre EL',         17),
    ('ESTRELLA LATINA',       'Yosbel Gutierrez B',        18),
    ('ESTRELLA LATINA',       'Luis A Toral C',            19),
    ('ESTRELLA LATINA',       'Hector Perez EL',           20),
    ('ESTRELLA LATINA',       'Geudys A Melendez B',       22),
    ('ESTRELLA LATINA',       'Christopher Guerrero EL',   23),
    ('ESTRELLA LATINA',       'Josue I Valdez G',          24),
    ('ESTRELLA LATINA',       'Radhanes Sosa EL',          27),
    ('ESTRELLA LATINA',       'Juan R Luna T',             29),
    ('ESTRELLA LATINA',       'Jorge M Garcia',            31),
    ('ESTRELLA LATINA',       'Eduardo R Liriano S',       32),
    ('ESTRELLA LATINA',       'Eddy Lora C',               34),
    ('ESTRELLA LATINA',       'Juan C Urena P',            35),
    ('ESTRELLA LATINA',       'Bryant Fernandez R',        38),
    ('ESTRELLA LATINA',       'Tim Spidel EL',             39),
    ('ESTRELLA LATINA',       'Luis O Alcantara C',        40),
    ('ESTRELLA LATINA',       'Hector Jr Perez R',         44),
    ('ESTRELLA LATINA',       'Hector Pena P',             50),
    ('ESTRELLA LATINA',       'Alexander Perez R',         50),  -- duplicado → ON CONFLICT ignora
    ('ESTRELLA LATINA',       'Gabriel D Oguisten',        56),
    ('ESTRELLA LATINA',       'Darwinn R Alcantar',        70),
    ('ESTRELLA LATINA',       'Endy E De La Rosa S',       72),
    ('ESTRELLA LATINA',       'Jose G Cruz',               88),
    -- ── LOS ANGELES DE MALAGA (18 visibles; 3 ocultos por corte de página) ──
    ('LOS ANGELES DE MALAGA', 'Victor E Castillo',         0),
    ('LOS ANGELES DE MALAGA', 'Ruben A Arenas R',          1),
    ('LOS ANGELES DE MALAGA', 'Anderson J Valdes A',       3),
    ('LOS ANGELES DE MALAGA', 'Ali R Garcia',              6),
    ('LOS ANGELES DE MALAGA', 'Luis H Salas G',            7),
    ('LOS ANGELES DE MALAGA', 'Yorbi Gutierrez AM',        8),
    ('LOS ANGELES DE MALAGA', 'Eimos A Urbina',            13),
    ('LOS ANGELES DE MALAGA', 'Jean A Silva',              16),
    ('LOS ANGELES DE MALAGA', 'Jose A Ortega G',           17),
    ('LOS ANGELES DE MALAGA', 'Reinaldo A Valero A',       18),
    ('LOS ANGELES DE MALAGA', 'Samuel Valdes AM',          20),
    ('LOS ANGELES DE MALAGA', 'Wilman E Cadenas',          21),
    ('LOS ANGELES DE MALAGA', 'Ruben D Arenas R',          22),
    ('LOS ANGELES DE MALAGA', 'Hector A Idrogo F',         24),
    ('LOS ANGELES DE MALAGA', 'Jose R Batista H',          27),
    ('LOS ANGELES DE MALAGA', 'Jose S Castro F',           28),
    ('LOS ANGELES DE MALAGA', 'Dickson M Diaz R',          33),
    ('LOS ANGELES DE MALAGA', 'Hector A Idrogo R',         99),
    -- ── MARBELLA (22 con dorsal; #31 duplicado → segundo ignorado) ──
    ('MARBELLA',              'Javier A Rojas O',          1),
    ('MARBELLA',              'Victor M Castillo M',       5),
    ('MARBELLA',              'Ernesto G Vargas E',        8),
    ('MARBELLA',              'Jose A Bruzal C',           12),
    ('MARBELLA',              'Orlando Acosta S',          14),
    ('MARBELLA',              'Jackson A Pantaleon R',     15),
    ('MARBELLA',              'Victor M Reyes R',          19),
    ('MARBELLA',              'Ernesto Prieto Herrero',    20),
    ('MARBELLA',              'Reinaldo D Contreras E',    21),
    ('MARBELLA',              'Hernan N Lopez',            22),
    ('MARBELLA',              'Luis A Lopez F',            23),
    ('MARBELLA',              'Anuar Chandi C',            24),
    ('MARBELLA',              'Jesus A Baez P',            27),
    ('MARBELLA',              'Junior D Gomez L',          31),
    ('MARBELLA',              'Eduardo D Suarez R',        31),  -- duplicado → ON CONFLICT ignora
    ('MARBELLA',              'Antonio F Garcia T',        34),
    ('MARBELLA',              'Luis A Asuaje Martinez',    47),
    ('MARBELLA',              'Yuri E Garcia F',           51),
    ('MARBELLA',              'Joaquin J Garcia V',        56),
    ('MARBELLA',              'Jhon A Mendoza M',          88),
    ('MARBELLA',              'Jose M Montilva C',         93),
    ('MARBELLA',              'Robert L Chacon M',         99),
    -- ── MARLINS DE MARBELLA (29 visibles; 3 ocultos por corte de página) ──
    ('MARLINS DE MARBELLA',   'Victor D Reyes M',          0),
    ('MARLINS DE MARBELLA',   'Juan C Vera B',             1),
    ('MARLINS DE MARBELLA',   'Starlyn D Santana',         2),
    ('MARLINS DE MARBELLA',   'Isaac Garcia MM',           3),
    ('MARLINS DE MARBELLA',   'Esnayling G Valdez',        4),
    ('MARLINS DE MARBELLA',   'Jonatan L Carvajal P',      5),
    ('MARLINS DE MARBELLA',   'Aramis J Perez',            6),
    ('MARLINS DE MARBELLA',   'Jose Fernandez T',          7),
    ('MARLINS DE MARBELLA',   'Winston Smith Salas B',     8),
    ('MARLINS DE MARBELLA',   'Gabriel A Ramirez R',       15),
    ('MARLINS DE MARBELLA',   'Kennedy Rodriguez B',       16),
    ('MARLINS DE MARBELLA',   'Yimel A Colom',             19),
    ('MARLINS DE MARBELLA',   'Joanel Stiwar Alcantara R', 21),
    ('MARLINS DE MARBELLA',   'Pedro A Colon H',           23),
    ('MARLINS DE MARBELLA',   'Emilio E Marte E',          26),
    ('MARLINS DE MARBELLA',   'Edwin M Loaisiga V',        31),
    ('MARLINS DE MARBELLA',   'Jhonmil Fuenmayor MM',      33),
    ('MARLINS DE MARBELLA',   'Francis J Ramirez M',       34),
    ('MARLINS DE MARBELLA',   'Danny F Martinez L',        39),
    ('MARLINS DE MARBELLA',   'Carlos M Perez N',          44),
    ('MARLINS DE MARBELLA',   'Carlos E de La Paz M',      45),
    ('MARLINS DE MARBELLA',   'Luis E Obrien C',           60),
    ('MARLINS DE MARBELLA',   'Jonathan M Nava R',         64),
    ('MARLINS DE MARBELLA',   'Jose De Los Santos Durant', 66),
    ('MARLINS DE MARBELLA',   'Luis Rotestan MM',          76),
    ('MARLINS DE MARBELLA',   'Carlos M Ramirez Santana',  80),
    ('MARLINS DE MARBELLA',   'Yordany Stephen M',         89),
    ('MARLINS DE MARBELLA',   'Ramon J Ugueto A',          90),
    ('MARLINS DE MARBELLA',   'Jose L Guzman C',           99),
    -- ── RANGERS DE TEXAS (16 visibles; 3 ocultos por corte de página) ──
    ('RANGERS DE TEXAS',      'Osvaldo Y Aramburo G',      0),
    ('RANGERS DE TEXAS',      'Wellintong M Lopez R',      3),
    ('RANGERS DE TEXAS',      'Jean C Elias',              6),
    ('RANGERS DE TEXAS',      'Michel Lorenzo Vega',       7),
    ('RANGERS DE TEXAS',      'Yoan J Machin D',           8),
    ('RANGERS DE TEXAS',      'Victor R Rodriguez C',      9),
    ('RANGERS DE TEXAS',      'Yirdo L Capote A',          10),
    ('RANGERS DE TEXAS',      'Miguel Machado',            11),
    ('RANGERS DE TEXAS',      'Carlos Cruz',               12),
    ('RANGERS DE TEXAS',      'Jose M Moreno',             13),
    ('RANGERS DE TEXAS',      'Ivo Carreras',              17),
    ('RANGERS DE TEXAS',      'Sebastian D Gelvis V',      19),
    ('RANGERS DE TEXAS',      'Gilbert R Hurtado',         22),
    ('RANGERS DE TEXAS',      'Elian Perez',               25),
    ('RANGERS DE TEXAS',      'Juan M Machin D',           28),
    ('RANGERS DE TEXAS',      'Elieser Garcia',            79),
    -- ── TIGRES DE MARBELLA (21 visibles; 1 oculto) ───────────
    ('TIGRES DE MARBELLA',    'Jose L Morales C',          0),
    ('TIGRES DE MARBELLA',    'Carlos G Fuente',           1),
    ('TIGRES DE MARBELLA',    'Elio R Villahermosa M',     4),
    ('TIGRES DE MARBELLA',    'Leonardo M Rosales R',      9),
    ('TIGRES DE MARBELLA',    'Ricardo R Amoldoni O',      10),
    ('TIGRES DE MARBELLA',    'Julio Sanchez TDM',         11),
    ('TIGRES DE MARBELLA',    'Luis G Barboza F',          12),
    ('TIGRES DE MARBELLA',    'Yair I Nalabanbachian',     14),
    ('TIGRES DE MARBELLA',    'Franklin J Sanchez R',      15),
    ('TIGRES DE MARBELLA',    'Carlos E Sanchez L',        16),
    ('TIGRES DE MARBELLA',    'Jose J Heredia P',          19),
    ('TIGRES DE MARBELLA',    'Yosmar M Zambrano',         21),
    ('TIGRES DE MARBELLA',    'Miguel A Medina G',         22),
    ('TIGRES DE MARBELLA',    'Renzo G Gonzales M',        23),
    ('TIGRES DE MARBELLA',    'Victor M Navas R',          27),
    ('TIGRES DE MARBELLA',    'Nestor J Caceres',          29),
    ('TIGRES DE MARBELLA',    'Miguel A Perez S',          31),
    ('TIGRES DE MARBELLA',    'Edgar J Padron Z',          41),
    ('TIGRES DE MARBELLA',    'Reynier E Gonzalez J',      45),
    ('TIGRES DE MARBELLA',    'Luis E Delpino T',          47),
    ('TIGRES DE MARBELLA',    'Wilmer J Contreras M',      99),
    -- ── TOROS DEL VISO (17 con dorsal; 4 ocultos por corte de página) ──
    ('TOROS DEL VISO',        'Daniel E Rivera R',         2),
    ('TOROS DEL VISO',        'Rhayfer J Moreno M',        3),
    ('TOROS DEL VISO',        'Yonaiker J Rodriguez H',    5),
    ('TOROS DEL VISO',        'David J Heriquez S',        7),
    ('TOROS DEL VISO',        'Gaspar M I Olea E',         10),
    ('TOROS DEL VISO',        'Herminio A Bello B',        11),
    ('TOROS DEL VISO',        'Orlando Doyen TV',          17),
    ('TOROS DEL VISO',        'Samuel Rodriguez',          18),
    ('TOROS DEL VISO',        'Brayan G Blanco P',         20),
    ('TOROS DEL VISO',        'David A Perez U',           23),
    ('TOROS DEL VISO',        'Ismael J Domenech C',       28),
    ('TOROS DEL VISO',        'Oscar Campos TDV',          33),
    ('TOROS DEL VISO',        'Luis E Machado B',          53),
    ('TOROS DEL VISO',        'Jesus F Vieira G',          61),
    ('TOROS DEL VISO',        'Hernan J Barrios L',        88),
    ('TOROS DEL VISO',        'Gustavo A Ferrer P',        89),
    ('TOROS DEL VISO',        'Adolfo Tigrero Lozano',     99),
    -- ── VENEMALAGA (17; roster completo) ─────────────────────
    ('VENEMALAGA',            'Rodolfo A Canelon',         8),
    ('VENEMALAGA',            'Jorge A Delgado',           9),
    ('VENEMALAGA',            'Alexander D Farias',        13),
    ('VENEMALAGA',            'Hemry A Guacaran',          14),
    ('VENEMALAGA',            'Nelson G Montilla',         17),
    ('VENEMALAGA',            'Yandri Mojeron Ramil',      19),
    ('VENEMALAGA',            'Pedro Urbina S',            21),
    ('VENEMALAGA',            'Luis Antonio Melchor',      22),
    ('VENEMALAGA',            'Keibert Cordoba VM',        23),
    ('VENEMALAGA',            'Daniel E Arellano',         27),
    ('VENEMALAGA',            'Celis A Sucre',             28),
    ('VENEMALAGA',            'David A Alvarez',           31),
    ('VENEMALAGA',            'Jefferson G Rosario',       33),
    ('VENEMALAGA',            'Rhamses Soto S',            37),
    ('VENEMALAGA',            'Orlando Gomez Pena',        57),
    ('VENEMALAGA',            'Jose Vicente Perdomo',      77),
    ('VENEMALAGA',            'Miguel Gonzalez G',         88),
    -- ── YANKEES DE MALAGA (22 visibles; 6 ocultos por corte de página) ──
    ('YANKEES DE MALAGA',     'Luis Velasquez Z',          0),
    ('YANKEES DE MALAGA',     'Luis M Anselmi M',          1),
    ('YANKEES DE MALAGA',     'Marco A Tapia',             2),
    ('YANKEES DE MALAGA',     'Shamir E Coa L',            3),
    ('YANKEES DE MALAGA',     'Jesus D Palacio C',         4),
    ('YANKEES DE MALAGA',     'Edward D Montilla C',       6),
    ('YANKEES DE MALAGA',     'Yesferi X Garrido',         16),
    ('YANKEES DE MALAGA',     'Wildemar Sam YM',           19),
    ('YANKEES DE MALAGA',     'Aureliano A Belen B',       21),
    ('YANKEES DE MALAGA',     'Jhonny L Pina A',           22),
    ('YANKEES DE MALAGA',     'Carlos R Padron R',         23),
    ('YANKEES DE MALAGA',     'Alexander J Cortesia V',    24),
    ('YANKEES DE MALAGA',     'Ricardo R Natera D',        25),
    ('YANKEES DE MALAGA',     'Emiliano J Mejias H',       26),
    ('YANKEES DE MALAGA',     'Henry L Trubuiani R',       27),
    ('YANKEES DE MALAGA',     'Pedro L Pirela',            32),
    ('YANKEES DE MALAGA',     'Pedro I Diaz S',            33),
    ('YANKEES DE MALAGA',     'Jose R Natera D',           43),
    ('YANKEES DE MALAGA',     'George I Verano',           44),
    ('YANKEES DE MALAGA',     'Alberto Cabrera M',         67),
    ('YANKEES DE MALAGA',     'Guillermo F Rodriguez P',   77),
    ('YANKEES DE MALAGA',     'Jhonatan J Escalante H',    99)
) AS v(team_name, full_name, jersey_number)
JOIN teams t ON t.name = v.team_name AND t.sport_type = 'SOFTBALL'
ON CONFLICT (team_id, jersey_number) DO NOTHING;

-- ── 4. Jugadores sin dorsal asignado ─────────────────────────
-- 3 jugadores del PDF no tienen número visible.
-- WHERE NOT EXISTS previene duplicados si el script se ejecutase más de una vez.

INSERT INTO players (full_name, jersey_number, team_id)
SELECT v.full_name, NULL, t.id
FROM (VALUES
    ('MARBELLA',       'Heaklyff Y Cardenas F'),
    ('TOROS DEL VISO', 'Carlos A Pantaleon C'),
    ('TOROS DEL VISO', 'Jose M Cordoba C')
) AS v(team_name, full_name)
JOIN teams t ON t.name = v.team_name AND t.sport_type = 'SOFTBALL'
WHERE NOT EXISTS (
    SELECT 1 FROM players p
    WHERE p.full_name = v.full_name AND p.team_id = t.id
);
