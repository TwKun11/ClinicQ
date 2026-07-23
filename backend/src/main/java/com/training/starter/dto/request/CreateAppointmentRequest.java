package com.training.starter.dto.request;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record CreateAppointmentRequest(
        @NotNull(message = "Patient is required")
        Long patientId,

        @NotNull(message = "Scheduled time is required")
        @FutureOrPresent(message = "Scheduled time cannot be in the past")
        LocalDateTime scheduledAt,

        @NotBlank(message = "Reason is required")
        @Size(max = 255)
        String reason,

        @Size(max = 20)
        String status,

        String note
) {}
