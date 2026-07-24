package com.training.starter.dto.response;

import java.time.LocalDateTime;

public record DoctorResponse(
        Long id,
        Long userId,
        String fullName,
        String specialty,
        String roomNumber,
        Integer maxPatientsPerDay,
        boolean active,
        LocalDateTime createdAt
) {}
