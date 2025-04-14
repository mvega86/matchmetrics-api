CREATE TABLE field_zone (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    min_x DOUBLE PRECISION NOT NULL,
    max_x DOUBLE PRECISION NOT NULL,
    min_y DOUBLE PRECISION NOT NULL,
    max_y DOUBLE PRECISION NOT NULL
);

-- ----------------------
-- X-AXIS ZONES (LENGTH, from right to left)
-- 0–16.18: Own area
-- 16.18–40.72: Defensive zone
-- 40.72–59.90: Midfield
-- 59.90–83.45: Offensive zone
-- 83.45–100: Opponent area
-- ----------------------
-- Y-AXIS ZONES (WIDTH)
-- 0–33: Right wing
-- 33–66: Middle
-- 66–100: Left wing

-- Own area
INSERT INTO field_zone (name, min_x, max_x, min_y, max_y)
VALUES
('Own area - Right wing', 0, 16.18, 0, 33),
('Own area - Middle', 0, 16.18, 33, 66),
('Own area - Left wing', 0, 16.18, 66, 100);

-- Defensive zone
INSERT INTO field_zone (name, min_x, max_x, min_y, max_y)
VALUES
('Defensive zone - Right wing', 16.18, 40.72, 0, 33),
('Defensive zone - Middle', 16.18, 40.72, 33, 66),
('Defensive zone - Left wing', 16.18, 40.72, 66, 100);

-- Midfield
INSERT INTO field_zone (name, min_x, max_x, min_y, max_y)
VALUES
('Midfield - Right wing', 40.72, 59, 0, 33),
('Midfield - Middle', 40.72, 59, 33, 66),
('Midfield - Left wing', 40.72, 59, 66, 100);

-- Offensive zone
INSERT INTO field_zone (name, min_x, max_x, min_y, max_y)
VALUES
('Offensive zone - Right wing', 59, 83.45, 0, 33),
('Offensive zone - Middle', 59, 83.45, 33, 66),
('Offensive zone - Left wing', 59, 83.45, 66, 100);

-- Opponent area
INSERT INTO field_zone (name, min_x, max_x, min_y, max_y)
VALUES
('Opponent area - Right wing', 83.45, 100, 0, 33),
('Opponent area - Middle', 83.45, 100, 33, 66),
('Opponent area - Left wing', 83.45, 100, 66, 100);
