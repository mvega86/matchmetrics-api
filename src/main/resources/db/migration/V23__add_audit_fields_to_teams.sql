ALTER TABLE teams
ADD COLUMN IF NOT EXISTS created_at TIMESTAMP,
ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP,
ADD COLUMN IF NOT EXISTS modified_by VARCHAR(255);

UPDATE teams
SET created_at = COALESCE(created_at, now()),
    updated_at = COALESCE(updated_at, now()),
    modified_by = COALESCE(modified_by, 'SYSTEM');