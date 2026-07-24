package com.training.starter.service.impl;

import com.training.starter.dto.request.CreateDoctorRequest;
import com.training.starter.dto.request.UpdateDoctorRequest;
import com.training.starter.dto.response.DoctorResponse;
import com.training.starter.entity.Doctor;
import com.training.starter.entity.User;
import com.training.starter.exception.BadRequestException;
import com.training.starter.exception.DuplicateResourceException;
import com.training.starter.exception.ResourceNotFoundException;
import com.training.starter.mapper.DoctorMapper;
import com.training.starter.repository.DoctorRepository;
import com.training.starter.repository.UserRepository;
import com.training.starter.service.DoctorService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DoctorServiceImpl implements DoctorService {

    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;
    private final DoctorMapper doctorMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<DoctorResponse> getAll(String specialty, Pageable pageable) {
        validateSpecialtySearch(specialty);
        Page<Doctor> doctors = specialty == null || specialty.isBlank()
                ? doctorRepository.findAll(pageable)
                : doctorRepository.findBySpecialtyContainingIgnoreCase(specialty.trim(), pageable);
        return doctors.map(doctorMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public DoctorResponse getById(Long id) {
        return doctorMapper.toResponse(findDoctor(id));
    }

    @Override
    @Transactional
    public DoctorResponse create(CreateDoctorRequest request) {
        if (doctorRepository.existsByUserId(request.userId())) {
            throw new DuplicateResourceException("Doctor", "userId", request.userId());
        }

        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new ResourceNotFoundException("User", request.userId()));

        Doctor doctor = doctorMapper.toEntity(request);
        doctor.setUser(user);
        if (request.maxPatientsPerDay() == null) {
            doctor.setMaxPatientsPerDay(20);
        }
        if (request.active() == null) {
            doctor.setActive(true);
        }
        return doctorMapper.toResponse(doctorRepository.save(doctor));
    }

    @Override
    @Transactional
    public DoctorResponse update(Long id, UpdateDoctorRequest request) {
        Doctor doctor = findDoctor(id);
        doctorMapper.updateEntity(doctor, request);
        return doctorMapper.toResponse(doctorRepository.save(doctor));
    }

    private Doctor findDoctor(Long id) {
        return doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", id));
    }

    private void validateSpecialtySearch(String specialty) {
        if (specialty == null || specialty.isBlank()) {
            return;
        }
        int length = specialty.trim().length();
        if (length < 2 || length > 100) {
            throw new BadRequestException("Specialty search must be between 2 and 100 characters");
        }
    }
}
