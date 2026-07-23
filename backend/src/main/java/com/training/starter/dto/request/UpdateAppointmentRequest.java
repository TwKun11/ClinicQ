package com.training.starter.dto.request;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record UpdateAppointmentRequest(
        Long patientId,

        @FutureOrPresent(message = "Scheduled time cannot be in the past")
        LocalDateTime scheduledAt,

        @Size(min = 1, max = 255)
        String reason,

        @Size(max = 20)
        String status,

        String note
) {}
