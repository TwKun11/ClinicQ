package com.training.starter.controller;

import com.training.starter.common.ApiResponse;
import com.training.starter.common.PageResponse;
import com.training.starter.common.SafePageRequest;
import com.training.starter.dto.request.CreateAppointmentRequest;
import com.training.starter.dto.response.AppointmentResponse;
import com.training.starter.service.AppointmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Tag(name = "Appointments", description = "Patient appointment booking operations")
public class AppointmentController {

    private static final Map<String, String> SORT_FIELDS = Map.of(
            "id", "id",
            "appointmentDate", "appointmentDate",
            "startTime", "startTime",
            "status", "status",
            "createdAt", "createdAt"
    );

    private final AppointmentService appointmentService;

    @GetMapping("/api/v1/appointments")
    @Operation(summary = "List current patient's appointments")
    public ApiResponse<PageResponse<AppointmentResponse>> getMine(
            @Parameter(description = "Zero-based page index")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size from 1 to 100")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Allowed values: id, appointmentDate, startTime, status, createdAt")
            @RequestParam(defaultValue = "appointmentDate") String sortBy,
            @Parameter(description = "Sort direction: ASC or DESC")
            @RequestParam(defaultValue = "DESC") String sortDir,
            Authentication authentication) {
        var pageable = SafePageRequest.of(page, size, sortBy, sortDir, SORT_FIELDS, "appointmentDate", Sort.Direction.DESC);
        var result = appointmentService.getMyAppointments(authentication.getName(), pageable);
        return ApiResponse.success(PageResponse.from(result, r -> r));
    }

    @GetMapping("/api/v1/appointments/{id}")
    @Operation(summary = "Get current patient's appointment by ID")
    public ApiResponse<AppointmentResponse> getById(@PathVariable Long id, Authentication authentication) {
        return ApiResponse.success(appointmentService.getMyAppointmentById(authentication.getName(), id));
    }

    @PostMapping("/api/v1/appointments")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Book an appointment for the current patient")
    public ApiResponse<AppointmentResponse> book(
            @Valid @RequestBody CreateAppointmentRequest request,
            Authentication authentication) {
        return ApiResponse.success("Appointment booked", appointmentService.bookAppointment(authentication.getName(), request));
    }

    @PutMapping("/api/v1/appointments/{id}/cancel")
    @Operation(summary = "Cancel current patient's scheduled appointment")
    public ApiResponse<AppointmentResponse> cancel(@PathVariable Long id, Authentication authentication) {
        return ApiResponse.success("Appointment cancelled", appointmentService.cancelAppointment(authentication.getName(), id));
    }

    @GetMapping("/api/v1/doctor/appointments")
    @Operation(summary = "List current doctor's appointments by date")
    public ApiResponse<PageResponse<AppointmentResponse>> getDoctorAppointments(
            @RequestParam(required = false) LocalDate date,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "startTime") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDir,
            Authentication authentication) {
        var pageable = SafePageRequest.of(page, size, sortBy, sortDir, SORT_FIELDS, "startTime", Sort.Direction.ASC);
        var result = appointmentService.getDoctorAppointments(authentication.getName(), date, pageable);
        return ApiResponse.success(PageResponse.from(result, r -> r));
    }
}
