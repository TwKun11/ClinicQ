package com.training.starter.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;

public record ScheduleSlotResponse(
        Long id,
        Long doctorId,
        String doctorName,
        LocalDate slotDate,
        LocalTime startTime,
        LocalTime endTime,
        String status
) {}
