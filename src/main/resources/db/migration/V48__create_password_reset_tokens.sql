CREATE TABLE IF NOT EXISTS password_reset_tokens (
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT       NOT NULL REFERENCES app_users(id) ON DELETE CASCADE,
    code       VARCHAR(6)   NOT NULL,
    method     VARCHAR(10)  NOT NULL,
    expires_at TIMESTAMP    NOT NULL,
    used       BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_prt_code ON password_reset_tokens(code);
CREATE INDEX IF NOT EXISTS idx_prt_user  ON password_reset_tokens(user_id);
