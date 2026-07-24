CREATE TABLE doctors (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE REFERENCES users(id),
    specialty VARCHAR(100) NOT NULL,
    room_number VARCHAR(10),
    max_patients_per_day INT NOT NULL DEFAULT 20,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_doctors_specialty ON doctors(specialty);
CREATE INDEX idx_doctors_active ON doctors(active);

INSERT INTO users (username, email, password, full_name, role, active)
VALUES
    ('doctor.lisa', 'lisa.martin@clinicq.local', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Dr. Lisa Martin', 'DOCTOR', TRUE),
    ('doctor.robert', 'robert.wilson@clinicq.local', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Dr. Robert Wilson', 'DOCTOR', TRUE),
    ('doctor.emily', 'emily.nguyen@clinicq.local', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Dr. Emily Nguyen', 'DOCTOR', TRUE)
ON CONFLICT (username) DO NOTHING;

INSERT INTO doctors (user_id, specialty, room_number, max_patients_per_day, active)
SELECT id, 'Cardiology', 'A101', 24, TRUE FROM users WHERE username = 'doctor.lisa'
ON CONFLICT (user_id) DO NOTHING;

INSERT INTO doctors (user_id, specialty, room_number, max_patients_per_day, active)
SELECT id, 'Traumatology', 'B202', 18, TRUE FROM users WHERE username = 'doctor.robert'
ON CONFLICT (user_id) DO NOTHING;

INSERT INTO doctors (user_id, specialty, room_number, max_patients_per_day, active)
SELECT id, 'Pediatrics', 'C303', 20, TRUE FROM users WHERE username = 'doctor.emily'
ON CONFLICT (user_id) DO NOTHING;
