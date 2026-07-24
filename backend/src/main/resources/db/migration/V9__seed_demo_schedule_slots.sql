INSERT INTO schedule_slots (doctor_id, slot_date, start_time, end_time, status, created_at, updated_at)
SELECT d.id, CURRENT_DATE, t.start_time, t.start_time + INTERVAL '30 minutes', 'AVAILABLE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM doctors d
CROSS JOIN (VALUES (TIME '08:30'), (TIME '09:30'), (TIME '10:30'), (TIME '13:30'), (TIME '14:30')) AS t(start_time)
WHERE d.active = TRUE
ON CONFLICT ON CONSTRAINT uk_schedule_slots_doctor_date_start DO NOTHING;

INSERT INTO schedule_slots (doctor_id, slot_date, start_time, end_time, status, created_at, updated_at)
SELECT d.id, CURRENT_DATE + INTERVAL '1 day', t.start_time, t.start_time + INTERVAL '30 minutes', 'AVAILABLE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM doctors d
CROSS JOIN (VALUES (TIME '08:30'), (TIME '09:30'), (TIME '10:30'), (TIME '13:30'), (TIME '14:30')) AS t(start_time)
WHERE d.active = TRUE
ON CONFLICT ON CONSTRAINT uk_schedule_slots_doctor_date_start DO NOTHING;
