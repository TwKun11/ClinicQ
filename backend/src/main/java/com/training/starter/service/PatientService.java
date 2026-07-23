package com.training.starter.service;

import com.training.starter.dto.request.CreatePatientRequest;
import com.training.starter.dto.request.UpdatePatientRequest;
import com.training.starter.dto.response.PatientResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PatientService {

    Page<PatientResponse> getAll(Pageable pageable);

    Page<PatientResponse> search(String search, Pageable pageable);

    PatientResponse getById(Long id);

    PatientResponse create(CreatePatientRequest request);

    PatientResponse update(Long id, UpdatePatientRequest request);

    void delete(Long id);
}
