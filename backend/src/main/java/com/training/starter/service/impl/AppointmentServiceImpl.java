package com.training.starter.service.impl;

import com.training.starter.dto.request.CreateAppointmentRequest;
import com.training.starter.dto.request.UpdateAppointmentRequest;
import com.training.starter.dto.response.AppointmentResponse;
import com.training.starter.entity.Appointment;
import com.training.starter.entity.Patient;
import com.training.starter.enums.AppointmentStatus;
import com.training.starter.exception.BadRequestException;
import com.training.starter.exception.ResourceNotFoundException;
import com.training.starter.mapper.AppointmentMapper;
import com.training.starter.repository.AppointmentRepository;
import com.training.starter.repository.PatientRepository;
import com.training.starter.repository.specification.AppointmentSpecifications;
import com.training.starter.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final AppointmentMapper appointmentMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<AppointmentResponse> getAll(Pageable pageable) {
        return appointmentRepository.findAll(pageable).map(appointmentMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AppointmentResponse> search(LocalDate date, Long patientId, String status, Pageable pageable) {
        AppointmentStatus appointmentStatus = resolveStatus(status, null);
        return appointmentRepository.findAll(AppointmentSpecifications.matchesFilters(date, patientId, appointmentStatus), pageable)
                .map(appointmentMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public AppointmentResponse getById(Long id) {
        Appointment appointment = findAppointment(id);
        return appointmentMapper.toResponse(appointment);
    }

    @Override
    @Transactional
    public AppointmentResponse create(CreateAppointmentRequest request) {
        Patient patient = findPatient(request.patientId());
        Appointment appointment = appointmentMapper.toEntity(request);
        appointment.setPatient(patient);
        appointment.setStatus(resolveStatus(request.status(), AppointmentStatus.SCHEDULED));
        return appointmentMapper.toResponse(appointmentRepository.save(appointment));
    }

    @Override
    @Transactional
    public AppointmentResponse update(Long id, UpdateAppointmentRequest request) {
        Appointment appointment = findAppointment(id);

        if (request.patientId() != null) {
            appointment.setPatient(findPatient(request.patientId()));
        }
        if (request.scheduledAt() != null) {
            appointment.setScheduledAt(request.scheduledAt());
        }
        if (request.reason() != null) {
            appointment.setReason(request.reason());
        }
        if (request.status() != null) {
            appointment.setStatus(resolveStatus(request.status(), appointment.getStatus()));
        }
        if (request.note() != null) {
            appointment.setNote(request.note());
        }

        return appointmentMapper.toResponse(appointmentRepository.save(appointment));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Appointment appointment = findAppointment(id);
        appointmentRepository.delete(appointment);
    }

    private Appointment findAppointment(Long id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", id));
    }

    private Patient findPatient(Long id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", id));
    }

    private AppointmentStatus resolveStatus(String value, AppointmentStatus defaultStatus) {
        if (value == null || value.isBlank()) {
            return defaultStatus;
        }
        try {
            return AppointmentStatus.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Invalid appointment status: " + value);
        }
    }

}
