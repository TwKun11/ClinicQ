UPDATE schedule_slots
SET version = 0
WHERE version IS NULL;

ALTER TABLE schedule_slots
ALTER COLUMN version SET DEFAULT 0;
