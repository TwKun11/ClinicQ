package com.training.starter.service.impl;

import com.training.starter.dto.request.CreateAppointmentRequest;
import com.training.starter.dto.response.AppointmentResponse;
import com.training.starter.entity.Appointment;
import com.training.starter.entity.ScheduleSlot;
import com.training.starter.entity.User;
import com.training.starter.enums.AppointmentStatus;
import com.training.starter.enums.ScheduleSlotStatus;
import com.training.starter.exception.BadRequestException;
import com.training.starter.exception.ResourceNotFoundException;
import com.training.starter.mapper.AppointmentMapper;
import com.training.starter.repository.AppointmentRepository;
import com.training.starter.repository.ScheduleSlotRepository;
import com.training.starter.repository.UserRepository;
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
    private final ScheduleSlotRepository scheduleSlotRepository;
    private final UserRepository userRepository;
    private final AppointmentMapper appointmentMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<AppointmentResponse> getMyAppointments(String username, Pageable pageable) {
        return appointmentRepository.findByPatientPrincipal(username, pageable)
                .map(appointmentMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public AppointmentResponse getMyAppointmentById(String username, Long id) {
        Appointment appointment = appointmentRepository.findByIdAndPatientPrincipal(id, username)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", id));
        return appointmentMapper.toResponse(appointment);
    }

    @Override
    @Transactional
    public AppointmentResponse bookAppointment(String username, CreateAppointmentRequest request) {
        User patient = findUser(username);
        ScheduleSlot slot = scheduleSlotRepository.findByIdForUpdate(request.slotId())
                .orElseThrow(() -> new ResourceNotFoundException("Schedule slot", request.slotId()));

        if (!slot.getDoctor().getId().equals(request.doctorId())) {
            throw new BadRequestException("Schedule slot does not belong to selected doctor");
        }
        if (!slot.getDoctor().isActive()) {
            throw new BadRequestException("Doctor is not accepting appointments");
        }
        if (slot.getStatus() != ScheduleSlotStatus.AVAILABLE) {
            throw new BadRequestException("Schedule slot is not available");
        }

        boolean overlaps = appointmentRepository
                .existsByPatientIdAndAppointmentDateAndStatusNotAndStartTimeLessThanAndEndTimeGreaterThan(
                        patient.getId(),
                        slot.getSlotDate(),
                        AppointmentStatus.CANCELLED,
                        slot.getEndTime(),
                        slot.getStartTime());
        if (overlaps) {
            throw new BadRequestException("Patient already has an appointment during this time");
        }

        slot.setStatus(ScheduleSlotStatus.BOOKED);
        Appointment appointment = Appointment.builder()
                .patient(patient)
                .doctor(slot.getDoctor())
                .slot(slot)
                .appointmentDate(slot.getSlotDate())
                .startTime(slot.getStartTime())
                .endTime(slot.getEndTime())
                .status(AppointmentStatus.SCHEDULED)
                .symptoms(request.symptoms())
                .notes(request.notes())
                .build();

        scheduleSlotRepository.save(slot);
        return appointmentMapper.toResponse(appointmentRepository.save(appointment));
    }

    @Override
    @Transactional
    public AppointmentResponse cancelAppointment(String username, Long id) {
        Appointment appointment = appointmentRepository.findByIdAndPatientPrincipal(id, username)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", id));
        if (appointment.getStatus() != AppointmentStatus.SCHEDULED) {
            throw new BadRequestException("Only scheduled appointments can be cancelled");
        }

        ScheduleSlot slot = scheduleSlotRepository.findByIdForUpdate(appointment.getSlot().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Schedule slot", appointment.getSlot().getId()));
        appointment.setStatus(AppointmentStatus.CANCELLED);
        slot.setStatus(ScheduleSlotStatus.AVAILABLE);

        scheduleSlotRepository.save(slot);
        return appointmentMapper.toResponse(appointmentRepository.save(appointment));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AppointmentResponse> getDoctorAppointments(String username, LocalDate date, Pageable pageable) {
        LocalDate appointmentDate = date == null ? LocalDate.now() : date;
        return appointmentRepository.findByDoctorPrincipalAndAppointmentDate(username, appointmentDate, pageable)
                .map(appointmentMapper::toResponse);
    }

    private User findUser(String username) {
        return userRepository.findByUsername(username)
                .or(() -> userRepository.findByEmail(username))
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username or email: " + username));
    }
}
