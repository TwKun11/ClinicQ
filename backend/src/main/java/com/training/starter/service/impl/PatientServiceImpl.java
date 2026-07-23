package com.training.starter.service.impl;

import com.training.starter.dto.request.CreatePatientRequest;
import com.training.starter.dto.request.UpdatePatientRequest;
import com.training.starter.dto.response.PatientResponse;
import com.training.starter.entity.Patient;
import com.training.starter.exception.BadRequestException;
import com.training.starter.exception.DuplicateResourceException;
import com.training.starter.exception.ResourceNotFoundException;
import com.training.starter.mapper.PatientMapper;
import com.training.starter.repository.PatientRepository;
import com.training.starter.repository.specification.PatientSpecifications;
import com.training.starter.service.PatientService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PatientServiceImpl implements PatientService {

    private final PatientRepository patientRepository;
    private final PatientMapper patientMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<PatientResponse> getAll(Pageable pageable) {
        return patientRepository.findAll(pageable).map(patientMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PatientResponse> search(String search, Pageable pageable) {
        validateSearch(search);
        return patientRepository.findAll(PatientSpecifications.matchesSearch(search), pageable)
                .map(patientMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public PatientResponse getById(Long id) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", id));
        return patientMapper.toResponse(patient);
    }

    @Override
    @Transactional
    public PatientResponse create(CreatePatientRequest request) {
        if (patientRepository.existsByPhone(request.phone())) {
            throw new DuplicateResourceException("Patient", "phone", request.phone());
        }
        if (request.email() != null && patientRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Patient", "email", request.email());
        }

        Patient patient = patientMapper.toEntity(request);
        return patientMapper.toResponse(patientRepository.save(patient));
    }

    @Override
    @Transactional
    public PatientResponse update(Long id, UpdatePatientRequest request) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", id));

        if (request.phone() != null && !request.phone().equals(patient.getPhone())
                && patientRepository.existsByPhone(request.phone())) {
            throw new DuplicateResourceException("Patient", "phone", request.phone());
        }
        if (request.email() != null && !request.email().equals(patient.getEmail())
                && patientRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Patient", "email", request.email());
        }

        patientMapper.updateEntity(patient, request);
        return patientMapper.toResponse(patientRepository.save(patient));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", id));
        patientRepository.delete(patient);
    }

    private void validateSearch(String search) {
        if (search == null || search.isBlank()) {
            return;
        }
        int length = search.trim().length();
        if (length < 3 || length > 100) {
            throw new BadRequestException("Search must be between 3 and 100 characters");
        }
    }
}
