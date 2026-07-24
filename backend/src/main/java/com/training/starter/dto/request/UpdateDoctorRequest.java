package com.training.starter.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record UpdateDoctorRequest(
        @Size(min = 1, max = 100)
        String specialty,

        @Size(max = 10)
        String roomNumber,

        @Min(value = 1, message = "Max patients per day must be at least 1")
        @Max(value = 200, message = "Max patients per day cannot exceed 200")
        Integer maxPatientsPerDay,

        Boolean active
) {}
