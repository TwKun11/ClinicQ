package com.training.starter.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreatePatientRequest(
        @NotBlank(message = "Full name is required")
        @Size(max = 100)
        String fullName,

        @NotBlank(message = "Phone is required")
        @Size(max = 20)
        String phone,

        @Email(message = "Email must be valid")
        @Size(max = 100)
        String email,

        @PastOrPresent(message = "Date of birth cannot be in the future")
        LocalDate dateOfBirth,

        @Size(max = 20)
        String gender,

        @Size(max = 255)
        String address,

        String medicalNote
) {}
