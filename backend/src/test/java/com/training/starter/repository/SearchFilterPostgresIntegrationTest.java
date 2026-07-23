package com.training.starter.repository;

import com.training.starter.BaseIntegrationTest;
import com.training.starter.common.SafePageRequest;
import com.training.starter.enums.AppointmentStatus;
import com.training.starter.repository.specification.AppointmentSpecifications;
import com.training.starter.repository.specification.PatientSpecifications;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class SearchFilterPostgresIntegrationTest extends BaseIntegrationTest {

    private static final Map<String, String> PATIENT_SORT_FIELDS = Map.of(
            "id", "id",
            "fullName", "fullName",
            "phone", "phone",
            "email", "email",
            "dateOfBirth", "dateOfBirth",
            "createdAt", "createdAt"
    );

    private static final Map<String, String> APPOINTMENT_SORT_FIELDS = Map.of(
            "id", "id",
            "scheduledAt", "scheduledAt",
            "status", "status",
            "createdAt", "createdAt",
            "patientId", "patient.id"
    );

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("TRUNCATE TABLE appointments, patients RESTART IDENTITY CASCADE");

        var now = LocalDateTime.now().minusDays(30);
        jdbcTemplate.batchUpdate("""
                INSERT INTO patients(full_name, phone, email, date_of_birth, gender, address, medical_note, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                IntStream.rangeClosed(1, 2_500)
                        .mapToObj(i -> new Object[]{
                                i % 250 == 0 ? "Nguyen Perf Target " + i : "Patient " + i,
                                "090" + String.format("%07d", i),
                                i % 333 == 0 ? "target" + i + "@clinicq.test" : "patient" + i + "@clinicq.test",
                                Date.valueOf(LocalDate.of(1990, 1, 1).plusDays(i % 10_000)),
                                i % 2 == 0 ? "MALE" : "FEMALE",
                                "District " + (i % 20),
                                "note " + i,
                                Timestamp.valueOf(now.plusMinutes(i)),
                                Timestamp.valueOf(now.plusMinutes(i))
                        })
                        .toList()
        );

        jdbcTemplate.batchUpdate("""
                INSERT INTO appointments(patient_id, scheduled_at, reason, status, note, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """,
                IntStream.rangeClosed(1, 5_000)
                        .mapToObj(i -> {
                            var scheduledAt = LocalDateTime.of(2026, 8, 1, 8, 0).plusMinutes(i * 15L);
                            return new Object[]{
                                    ((i - 1) % 2_500) + 1L,
                                    Timestamp.valueOf(scheduledAt),
                                    "Checkup " + i,
                                    i % 3 == 0 ? "COMPLETED" : i % 3 == 1 ? "SCHEDULED" : "CANCELLED",
                                    "appointment note " + i,
                                    Timestamp.valueOf(scheduledAt.minusDays(7)),
                                    Timestamp.valueOf(scheduledAt.minusDays(7))
                            };
                        })
                        .toList()
        );
    }

    @Test
    void patientSearch_usesRealPostgresDataAndIndexedPlan() {
        var pageable = SafePageRequest.of(0, 20, "createdAt", "DESC",
                PATIENT_SORT_FIELDS, "createdAt", Sort.Direction.DESC);

        var result = patientRepository.findAll(PatientSpecifications.matchesSearch("target"), pageable);

        assertThat(result.getContent()).isNotEmpty();
        assertThat(result.getContent()).allSatisfy(patient ->
                assertThat((patient.getFullName() + " " + patient.getEmail()).toLowerCase()).contains("target"));

        var plan = explain("""
                SELECT id FROM patients
                WHERE lower(full_name) LIKE '%target%' ESCAPE '\\'
                   OR lower(phone) LIKE '%target%' ESCAPE '\\'
                   OR lower(email) LIKE '%target%' ESCAPE '\\'
                ORDER BY created_at DESC, id DESC
                LIMIT 20
                """);

        assertThat(plan).containsAnyOf(
                "idx_patients_full_name_trgm",
                "idx_patients_phone_trgm",
                "idx_patients_email_trgm"
        );
    }

    @Test
    void appointmentFilters_useRealPostgresDataAndCompositeIndexes() {
        var patientId = 42L;
        var date = LocalDate.of(2026, 8, 1).plusDays(26);
        var pageable = SafePageRequest.of(0, 20, "scheduledAt", "ASC",
                APPOINTMENT_SORT_FIELDS, "scheduledAt", Sort.Direction.ASC);

        var result = appointmentRepository.findAll(
                AppointmentSpecifications.matchesFilters(date, patientId, AppointmentStatus.SCHEDULED),
                pageable
        );

        assertThat(result.getContent()).allSatisfy(appointment -> {
            assertThat(appointment.getPatient().getId()).isEqualTo(patientId);
            assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.SCHEDULED);
            assertThat(appointment.getScheduledAt().toLocalDate()).isEqualTo(date);
        });

        var plan = explain("""
                SELECT id FROM appointments
                WHERE patient_id = 42
                  AND status = 'SCHEDULED'
                  AND scheduled_at >= TIMESTAMP '2026-08-27 00:00:00'
                  AND scheduled_at < TIMESTAMP '2026-08-28 00:00:00'
                ORDER BY scheduled_at ASC, id ASC
                LIMIT 20
                """);

        assertThat(plan).contains("idx_appointments_patient_status_scheduled_at_id");
    }

    private String explain(String sql) {
        jdbcTemplate.execute("SET enable_seqscan = off");
        return String.join("\n", jdbcTemplate.queryForList("EXPLAIN " + sql, String.class));
    }
}
