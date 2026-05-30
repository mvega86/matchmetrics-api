INSERT INTO app_users (
    email,
    password,
    full_name,
    provider,
    role,
    status,
    team_id,
    requested_team_name,
    created_at,
    updated_at,
    modified_by
)
SELECT
    'admin@matchmetrics.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'Administrador MatchMetrics',
    'LOCAL',
    'ADMIN',
    'APPROVED',
    NULL,
    NULL,
    now(),
    now(),
    'SYSTEM'
WHERE NOT EXISTS (
    SELECT 1
    FROM app_users
    WHERE email = 'admin@matchmetrics.com'
);