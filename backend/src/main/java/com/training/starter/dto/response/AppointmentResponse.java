package com.training.starter.dto.response;

import java.time.LocalDateTime;

public record AppointmentResponse(
        Long id,
        Long patientId,
        String patientName,
        LocalDateTime scheduledAt,
        String reason,
        String status,
        String note,
        LocalDateTime createdAt
) {}
