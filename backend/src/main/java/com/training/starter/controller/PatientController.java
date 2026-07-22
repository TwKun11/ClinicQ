package com.training.starter.controller;

import com.training.starter.common.ApiResponse;
import com.training.starter.common.PageResponse;
import com.training.starter.dto.request.CreatePatientRequest;
import com.training.starter.dto.request.UpdatePatientRequest;
import com.training.starter.dto.response.PatientResponse;
import com.training.starter.service.PatientService;
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
@RequestMapping("/api/v1/patients")
@RequiredArgsConstructor
@Tag(name = "Patients", description = "Patient CRUD operations")
public class PatientController {

    private final PatientService patientService;

    @GetMapping
    @Operation(summary = "List all patients with pagination")
    public ApiResponse<PageResponse<PatientResponse>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        var result = patientService.getAll(PageRequest.of(page, size, sort));
        return ApiResponse.success(PageResponse.from(result, r -> r));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get patient by ID")
    public ApiResponse<PatientResponse> getById(@PathVariable Long id) {
        return ApiResponse.success(patientService.getById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new patient")
    public ApiResponse<PatientResponse> create(@Valid @RequestBody CreatePatientRequest request) {
        return ApiResponse.success("Patient created", patientService.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing patient")
    public ApiResponse<PatientResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePatientRequest request) {
        return ApiResponse.success("Patient updated", patientService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a patient")
    public void delete(@PathVariable Long id) {
        patientService.delete(id);
    }
}
