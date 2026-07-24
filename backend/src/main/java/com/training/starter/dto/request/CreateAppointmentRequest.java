package com.training.starter.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateAppointmentRequest(
        @NotNull(message = "Doctor is required")
        Long doctorId,

        @NotNull(message = "Schedule slot is required")
        Long slotId,

        String symptoms,

        @Size(max = 1000)
        String notes
) {}
