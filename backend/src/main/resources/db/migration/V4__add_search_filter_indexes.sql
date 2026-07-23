CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE INDEX idx_patients_full_name_trgm ON patients USING GIN (lower(full_name) gin_trgm_ops);
CREATE INDEX idx_patients_phone_trgm ON patients USING GIN (lower(phone) gin_trgm_ops);
CREATE INDEX idx_patients_email_trgm ON patients USING GIN (lower(email) gin_trgm_ops);

DROP INDEX IF EXISTS idx_patients_phone;
DROP INDEX IF EXISTS idx_patients_email;
CREATE INDEX idx_patients_created_at_id ON patients(created_at DESC, id DESC);

DROP INDEX IF EXISTS idx_appointments_patient_id;
DROP INDEX IF EXISTS idx_appointments_scheduled_at;
DROP INDEX IF EXISTS idx_appointments_status;

CREATE INDEX idx_appointments_scheduled_at_id ON appointments(scheduled_at, id);
CREATE INDEX idx_appointments_patient_scheduled_at_id ON appointments(patient_id, scheduled_at, id);
CREATE INDEX idx_appointments_status_scheduled_at_id ON appointments(status, scheduled_at, id);
CREATE INDEX idx_appointments_patient_status_scheduled_at_id ON appointments(patient_id, status, scheduled_at, id);
