package com.training.starter.controller;

import com.training.starter.common.ApiResponse;
import com.training.starter.common.PageResponse;
import com.training.starter.common.SafePageRequest;
import com.training.starter.dto.request.CreateAppointmentRequest;
import com.training.starter.dto.request.UpdateAppointmentRequest;
import com.training.starter.dto.response.AppointmentResponse;
import com.training.starter.service.AppointmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/appointments")
@RequiredArgsConstructor
@Tag(name = "Appointments", description = "Appointment CRUD operations")
public class AppointmentController {

    private static final Map<String, String> SORT_FIELDS = Map.of(
            "id", "id",
            "scheduledAt", "scheduledAt",
            "status", "status",
            "createdAt", "createdAt",
            "patientId", "patient.id"
    );

    private final AppointmentService appointmentService;

    @GetMapping
    @Operation(summary = "List appointments with pagination, filters, and safe sorting")
    public ApiResponse<PageResponse<AppointmentResponse>> getAll(
            @Parameter(description = "Zero-based page index")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size from 1 to 100")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Allowed values: id, scheduledAt, status, createdAt, patientId")
            @RequestParam(defaultValue = "scheduledAt") String sortBy,
            @Parameter(description = "Sort direction: ASC or DESC")
            @RequestParam(defaultValue = "ASC") String sortDir,
            @Parameter(description = "Optional appointment date filter in ISO format yyyy-MM-dd")
            @RequestParam(required = false) LocalDate date,
            @Parameter(description = "Optional patient id filter")
            @RequestParam(required = false) Long patientId,
            @Parameter(description = "Optional status filter. Allowed values: SCHEDULED, COMPLETED, CANCELLED")
            @RequestParam(required = false) String status) {
        var pageable = SafePageRequest.of(page, size, sortBy, sortDir, SORT_FIELDS, "scheduledAt", Sort.Direction.ASC);
        var result = appointmentService.search(date, patientId, status, pageable);
        return ApiResponse.success(PageResponse.from(result, r -> r));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get appointment by ID")
    public ApiResponse<AppointmentResponse> getById(@PathVariable Long id) {
        return ApiResponse.success(appointmentService.getById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new appointment")
    public ApiResponse<AppointmentResponse> create(@Valid @RequestBody CreateAppointmentRequest request) {
        return ApiResponse.success("Appointment created", appointmentService.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing appointment")
    public ApiResponse<AppointmentResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateAppointmentRequest request) {
        return ApiResponse.success("Appointment updated", appointmentService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete an appointment")
    public void delete(@PathVariable Long id) {
        appointmentService.delete(id);
    }

}
