package com.training.starter.repository;

import com.training.starter.entity.Appointment;
import com.training.starter.enums.AppointmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long>, JpaSpecificationExecutor<Appointment> {

    boolean existsByPatientIdAndAppointmentDateAndStatusNotAndStartTimeLessThanAndEndTimeGreaterThan(
            Long patientId,
            LocalDate appointmentDate,
            AppointmentStatus excludedStatus,
            LocalTime endTime,
            LocalTime startTime
    );

    Page<Appointment> findByPatientUsername(String username, Pageable pageable);

    Optional<Appointment> findByIdAndPatientUsername(Long id, String username);

    Page<Appointment> findByDoctorUserUsernameAndAppointmentDate(String username, LocalDate appointmentDate, Pageable pageable);
}
