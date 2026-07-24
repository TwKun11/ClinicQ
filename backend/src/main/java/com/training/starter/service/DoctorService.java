package com.training.starter.service;

import com.training.starter.dto.request.CreateDoctorRequest;
import com.training.starter.dto.request.UpdateDoctorRequest;
import com.training.starter.dto.response.DoctorResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DoctorService {

    Page<DoctorResponse> getAll(String specialty, Pageable pageable);

    DoctorResponse getById(Long id);

    DoctorResponse create(CreateDoctorRequest request);

    DoctorResponse update(Long id, UpdateDoctorRequest request);
}
