ALTER TABLE messages ADD COLUMN IF NOT EXISTS receiver_id BIGINT REFERENCES users(id);
ALTER TABLE messages ADD COLUMN IF NOT EXISTS message_type VARCHAR(40);
ALTER TABLE messages ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;

UPDATE messages
SET message_type = CASE
  WHEN event_id IS NOT NULL THEN 'EVENT'
  WHEN team_id IS NOT NULL THEN 'TEAM'
  ELSE 'GLOBAL'
END
WHERE message_type IS NULL;

ALTER TABLE messages ALTER COLUMN message_type SET DEFAULT 'GLOBAL';
ALTER TABLE messages ALTER COLUMN message_type SET NOT NULL;

CREATE INDEX IF NOT EXISTS idx_messages_direct ON messages(sender_id, receiver_id, sent_at);
CREATE INDEX IF NOT EXISTS idx_messages_team ON messages(team_id, sent_at);
CREATE INDEX IF NOT EXISTS idx_messages_event ON messages(event_id, sent_at);
