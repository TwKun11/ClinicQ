package com.training.starter.repository;

import com.training.starter.BaseIntegrationTest;
import com.training.starter.common.SafePageRequest;
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

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PatientRepository patientRepository;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("TRUNCATE TABLE appointments, schedule_slots, patients RESTART IDENTITY CASCADE");

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

    private String explain(String sql) {
        jdbcTemplate.execute("SET enable_seqscan = off");
        return String.join("\n", jdbcTemplate.queryForList("EXPLAIN " + sql, String.class));
    }
}
