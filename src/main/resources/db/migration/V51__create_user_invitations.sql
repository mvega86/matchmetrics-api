CREATE TABLE user_invitations (
    id          BIGSERIAL    PRIMARY KEY,
    email       VARCHAR(150) NOT NULL,
    token       VARCHAR(255) NOT NULL UNIQUE,
    expires_at  TIMESTAMP    NOT NULL,
    used        BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP,
    updated_at  TIMESTAMP,
    modified_by VARCHAR(255) DEFAULT 'SYSTEM'
);

CREATE INDEX idx_user_invitations_token ON user_invitations(token);
CREATE INDEX idx_user_invitations_email ON user_invitations(email);
