package com.training.starter.controller;

import com.training.starter.common.ApiResponse;
import com.training.starter.common.PageResponse;
import com.training.starter.common.SafePageRequest;
import com.training.starter.dto.request.CreateDoctorRequest;
import com.training.starter.dto.request.UpdateDoctorRequest;
import com.training.starter.dto.response.DoctorResponse;
import com.training.starter.service.DoctorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
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
@RequiredArgsConstructor
@Tag(name = "Doctors", description = "Doctor profiles and public doctor discovery")
public class DoctorController {

    private static final Map<String, String> SORT_FIELDS = Map.of(
            "id", "id",
            "specialty", "specialty",
            "roomNumber", "roomNumber",
            "maxPatientsPerDay", "maxPatientsPerDay",
            "active", "active",
            "createdAt", "createdAt"
    );

    private final DoctorService doctorService;

    @GetMapping("/api/v1/doctors")
    @Operation(summary = "List doctors by optional specialty")
    public ApiResponse<PageResponse<DoctorResponse>> getAll(
            @Parameter(description = "Zero-based page index")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size from 1 to 100")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Allowed values: id, specialty, roomNumber, maxPatientsPerDay, active, createdAt")
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction: ASC or DESC")
            @RequestParam(defaultValue = "ASC") String sortDir,
            @Parameter(description = "Optional specialty filter")
            @RequestParam(required = false) String specialty) {
        var pageable = SafePageRequest.of(page, size, sortBy, sortDir, SORT_FIELDS, "createdAt", Sort.Direction.ASC);
        var result = doctorService.getAll(specialty, pageable);
        return ApiResponse.success(PageResponse.from(result, r -> r));
    }

    @GetMapping("/api/v1/doctors/{id}")
    @Operation(summary = "Get doctor detail")
    public ApiResponse<DoctorResponse> getById(@PathVariable Long id) {
        return ApiResponse.success(doctorService.getById(id));
    }

    @PostMapping("/api/v1/admin/doctors")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create doctor profile")
    public ApiResponse<DoctorResponse> create(@Valid @RequestBody CreateDoctorRequest request) {
        return ApiResponse.success("Doctor created", doctorService.create(request));
    }

    @PutMapping("/api/v1/admin/doctors/{id}")
    @Operation(summary = "Update doctor profile")
    public ApiResponse<DoctorResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateDoctorRequest request) {
        return ApiResponse.success("Doctor updated", doctorService.update(id, request));
    }
}
