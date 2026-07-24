package com.training.starter.repository;

import com.training.starter.entity.ScheduleSlot;
import com.training.starter.enums.ScheduleSlotStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ScheduleSlotRepository extends JpaRepository<ScheduleSlot, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from ScheduleSlot s join fetch s.doctor d join fetch d.user where s.id = :id")
    Optional<ScheduleSlot> findByIdForUpdate(@Param("id") Long id);

    List<ScheduleSlot> findByDoctorIdAndSlotDateAndStatusOrderByStartTime(
            Long doctorId,
            LocalDate slotDate,
            ScheduleSlotStatus status
    );

    boolean existsByDoctorIdAndSlotDateAndStartTime(Long doctorId, LocalDate slotDate, LocalTime startTime);
}
