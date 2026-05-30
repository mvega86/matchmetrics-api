CREATE TABLE app_users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(150) NOT NULL UNIQUE,
    password VARCHAR(255),
    full_name VARCHAR(150) NOT NULL,
    provider VARCHAR(50) NOT NULL DEFAULT 'LOCAL',
    role VARCHAR(30) NOT NULL DEFAULT 'USER',
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    team_id BIGINT,
    requested_team_name VARCHAR(150),
    created_at TIMESTAMP DEFAULT now(),
    updated_at TIMESTAMP DEFAULT now(),
    modified_by VARCHAR(100),
    CONSTRAINT fk_app_users_team FOREIGN KEY (team_id) REFERENCES teams(id)
);