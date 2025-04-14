-- Add column to relate to the field_zone table
ALTER TABLE player_statistic
ADD COLUMN field_zone_id BIGINT;

-- Create the foreign key
ALTER TABLE player_statistic
ADD CONSTRAINT fk_player_statistic_field_zone
FOREIGN KEY (field_zone_id)
REFERENCES field_zone(id)
ON DELETE SET NULL;