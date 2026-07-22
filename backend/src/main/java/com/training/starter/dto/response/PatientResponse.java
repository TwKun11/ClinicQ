package com.training.starter.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record PatientResponse(
        Long id,
        String fullName,
        String phone,
        String email,
        LocalDate dateOfBirth,
        String gender,
        String address,
        String medicalNote,
        LocalDateTime createdAt
) {}
