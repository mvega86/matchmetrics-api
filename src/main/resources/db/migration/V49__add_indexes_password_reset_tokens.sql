-- Índices compuestos para optimizar las consultas más frecuentes del flujo de reset
CREATE INDEX IF NOT EXISTS idx_prt_email_code_used
    ON password_reset_tokens (user_id, code, used);

CREATE INDEX IF NOT EXISTS idx_prt_created_at
    ON password_reset_tokens (created_at);

CREATE INDEX IF NOT EXISTS idx_prt_expires_at
    ON password_reset_tokens (expires_at);
