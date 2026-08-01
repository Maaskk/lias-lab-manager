ALTER TABLE members ADD COLUMN IF NOT EXISTS affiliation_date DATE;

ALTER TABLE membership_requests ADD COLUMN IF NOT EXISTS requested_type VARCHAR(80);
ALTER TABLE membership_requests ADD COLUMN IF NOT EXISTS establishment VARCHAR(255);
ALTER TABLE membership_requests ADD COLUMN IF NOT EXISTS origin_lab VARCHAR(255);
ALTER TABLE membership_requests ADD COLUMN IF NOT EXISTS interests TEXT;
ALTER TABLE membership_requests ADD COLUMN IF NOT EXISTS biography TEXT;
ALTER TABLE membership_requests ADD COLUMN IF NOT EXISTS preferred_team_id BIGINT;
ALTER TABLE membership_requests ADD COLUMN IF NOT EXISTS phone VARCHAR(120);
