package com.training.starter.repository;

import com.training.starter.entity.Appointment;
import com.training.starter.enums.AppointmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    @Query("select a from Appointment a where a.patient.username = :principal or a.patient.email = :principal")
    Page<Appointment> findByPatientPrincipal(@Param("principal") String principal, Pageable pageable);

    Optional<Appointment> findByIdAndPatientUsername(Long id, String username);

    @Query("""
            select a from Appointment a
            where a.id = :id and (a.patient.username = :principal or a.patient.email = :principal)
            """)
    Optional<Appointment> findByIdAndPatientPrincipal(@Param("id") Long id, @Param("principal") String principal);

    Page<Appointment> findByDoctorUserUsernameAndAppointmentDate(String username, LocalDate appointmentDate, Pageable pageable);

    @Query("""
            select a from Appointment a
            where (a.doctor.user.username = :principal or a.doctor.user.email = :principal)
              and a.appointmentDate = :appointmentDate
            """)
    Page<Appointment> findByDoctorPrincipalAndAppointmentDate(
            @Param("principal") String principal,
            @Param("appointmentDate") LocalDate appointmentDate,
            Pageable pageable);
}
