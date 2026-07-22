package com.training.starter.controller;

import com.training.starter.common.ApiResponse;
import com.training.starter.common.PageResponse;
import com.training.starter.dto.request.CreateAppointmentRequest;
import com.training.starter.dto.request.UpdateAppointmentRequest;
import com.training.starter.dto.response.AppointmentResponse;
import com.training.starter.service.AppointmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
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

@RestController
@RequestMapping("/api/v1/appointments")
@RequiredArgsConstructor
@Tag(name = "Appointments", description = "Appointment CRUD operations")
public class AppointmentController {

    private final AppointmentService appointmentService;

    @GetMapping
    @Operation(summary = "List all appointments with pagination")
    public ApiResponse<PageResponse<AppointmentResponse>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "scheduledAt") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDir) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        var result = appointmentService.getAll(PageRequest.of(page, size, sort));
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
