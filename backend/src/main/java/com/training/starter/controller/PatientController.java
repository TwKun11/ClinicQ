package com.training.starter.controller;

import com.training.starter.common.ApiResponse;
import com.training.starter.common.PageResponse;
import com.training.starter.common.SafePageRequest;
import com.training.starter.dto.request.CreatePatientRequest;
import com.training.starter.dto.request.UpdatePatientRequest;
import com.training.starter.dto.response.PatientResponse;
import com.training.starter.service.PatientService;
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

import java.util.Map;

@RestController
@RequestMapping("/api/v1/patients")
@RequiredArgsConstructor
@Tag(name = "Patients", description = "Patient CRUD operations")
public class PatientController {

    private static final Map<String, String> SORT_FIELDS = Map.of(
            "id", "id",
            "fullName", "fullName",
            "phone", "phone",
            "email", "email",
            "dateOfBirth", "dateOfBirth",
            "createdAt", "createdAt"
    );

    private final PatientService patientService;

    @GetMapping
    @Operation(summary = "List patients with pagination, search, and safe sorting")
    public ApiResponse<PageResponse<PatientResponse>> getAll(
            @Parameter(description = "Zero-based page index")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size from 1 to 100")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Allowed values: id, fullName, phone, email, dateOfBirth, createdAt")
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction: ASC or DESC")
            @RequestParam(defaultValue = "DESC") String sortDir,
            @Parameter(description = "Optional case-insensitive literal substring search across fullName, phone, and email. Nonblank values must be 3 to 100 characters.")
            @RequestParam(required = false) String search) {
        var pageable = SafePageRequest.of(page, size, sortBy, sortDir, SORT_FIELDS, "createdAt", Sort.Direction.DESC);
        var result = patientService.search(search, pageable);
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
