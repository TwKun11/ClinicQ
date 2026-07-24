INSERT INTO users (username, email, password, full_name, role, active, status, created_at, updated_at)
VALUES (
    'admin',
    'admin@clinicq.local',
    '$2a$10$fZGXXu8bOXKy0T/yT9VLE.fW.saaNjJcwOLAmpLKFc1.78hCYiynq',
    'Admin User',
    'ADMIN',
    true,
    'ACTIVE',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
)
ON CONFLICT (email) DO UPDATE
SET
    username = EXCLUDED.username,
    password = EXCLUDED.password,
    full_name = EXCLUDED.full_name,
    role = EXCLUDED.role,
    active = EXCLUDED.active,
    status = EXCLUDED.status,
    updated_at = CURRENT_TIMESTAMP;
