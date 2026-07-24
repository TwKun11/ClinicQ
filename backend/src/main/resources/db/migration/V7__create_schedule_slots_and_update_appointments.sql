CREATE TABLE schedule_slots (
    id BIGSERIAL PRIMARY KEY,
    doctor_id BIGINT NOT NULL,
    slot_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
    version BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_schedule_slots_doctor FOREIGN KEY (doctor_id) REFERENCES doctors(id),
    CONSTRAINT uk_schedule_slots_doctor_date_start UNIQUE (doctor_id, slot_date, start_time)
);

ALTER TABLE appointments DROP CONSTRAINT IF EXISTS fk_appointments_patient;
ALTER TABLE appointments ALTER COLUMN scheduled_at DROP NOT NULL;
ALTER TABLE appointments ALTER COLUMN reason DROP NOT NULL;
ALTER TABLE appointments ADD COLUMN doctor_id BIGINT;
ALTER TABLE appointments ADD COLUMN slot_id BIGINT;
ALTER TABLE appointments ADD COLUMN appointment_date DATE;
ALTER TABLE appointments ADD COLUMN start_time TIME;
ALTER TABLE appointments ADD COLUMN end_time TIME;
ALTER TABLE appointments ADD COLUMN symptoms TEXT;
ALTER TABLE appointments ADD COLUMN notes TEXT;

UPDATE appointments
SET appointment_date = DATE(scheduled_at),
    start_time = CAST(scheduled_at AS TIME),
    end_time = CAST(scheduled_at AS TIME) + INTERVAL '30 minutes',
    symptoms = reason,
    notes = note
WHERE scheduled_at IS NOT NULL;

ALTER TABLE appointments ADD CONSTRAINT fk_appointments_patient_user FOREIGN KEY (patient_id) REFERENCES users(id);
ALTER TABLE appointments ADD CONSTRAINT fk_appointments_doctor FOREIGN KEY (doctor_id) REFERENCES doctors(id);
ALTER TABLE appointments ADD CONSTRAINT fk_appointments_slot FOREIGN KEY (slot_id) REFERENCES schedule_slots(id);

CREATE INDEX idx_schedule_slots_doctor_date_status ON schedule_slots(doctor_id, slot_date, status);
CREATE INDEX idx_appointments_patient_date ON appointments(patient_id, appointment_date);
CREATE INDEX idx_appointments_doctor_date ON appointments(doctor_id, appointment_date);
