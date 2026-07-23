package com.training.starter.service;

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
import com.training.starter.service.impl.AppointmentServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private AppointmentMapper appointmentMapper;

    @InjectMocks
    private AppointmentServiceImpl appointmentService;

    @Test
    void create_validRequest_returnsAppointmentResponse() {
        var scheduledAt = LocalDateTime.now().plusDays(1);
        var request = new CreateAppointmentRequest(1L, scheduledAt, "Annual checkup", null, "Bring lab results");
        var patient = buildPatient(1L);
        var appointment = buildAppointment(1L, patient, scheduledAt, AppointmentStatus.SCHEDULED);
        var response = buildResponse(1L, 1L, scheduledAt, "SCHEDULED");

        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(appointmentMapper.toEntity(request)).thenReturn(appointment);
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);
        when(appointmentMapper.toResponse(appointment)).thenReturn(response);

        var result = appointmentService.create(request);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.status()).isEqualTo("SCHEDULED");
        verify(appointmentRepository).save(appointment);
    }

    @Test
    void create_patientNotFound_throwsResourceNotFoundException() {
        var request = new CreateAppointmentRequest(999L, LocalDateTime.now().plusDays(1), "Checkup", null, null);
        when(patientRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appointmentService.create(request))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void create_invalidStatus_throwsBadRequestException() {
        var request = new CreateAppointmentRequest(1L, LocalDateTime.now().plusDays(1), "Checkup", "waiting", null);
        var patient = buildPatient(1L);
        var appointment = buildAppointment(null, patient, request.scheduledAt(), null);

        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(appointmentMapper.toEntity(request)).thenReturn(appointment);

        assertThatThrownBy(() -> appointmentService.create(request))
                .isInstanceOf(BadRequestException.class);
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void getById_found_returnsAppointmentResponse() {
        var scheduledAt = LocalDateTime.now().plusDays(1);
        var patient = buildPatient(1L);
        var appointment = buildAppointment(1L, patient, scheduledAt, AppointmentStatus.SCHEDULED);
        var response = buildResponse(1L, 1L, scheduledAt, "SCHEDULED");

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(appointmentMapper.toResponse(appointment)).thenReturn(response);

        var result = appointmentService.getById(1L);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.patientId()).isEqualTo(1L);
    }

    @Test
    void update_validRequest_updatesAndReturns() {
        var scheduledAt = LocalDateTime.now().plusDays(1);
        var newScheduledAt = LocalDateTime.now().plusDays(2);
        var patient = buildPatient(1L);
        var appointment = buildAppointment(1L, patient, scheduledAt, AppointmentStatus.SCHEDULED);
        var request = new UpdateAppointmentRequest(null, newScheduledAt, "Follow up", "completed", "Done");
        var response = buildResponse(1L, 1L, newScheduledAt, "COMPLETED");

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(appointment)).thenReturn(appointment);
        when(appointmentMapper.toResponse(appointment)).thenReturn(response);

        var result = appointmentService.update(1L, request);

        assertThat(result.status()).isEqualTo("COMPLETED");
        assertThat(appointment.getScheduledAt()).isEqualTo(newScheduledAt);
        assertThat(appointment.getReason()).isEqualTo("Follow up");
        verify(appointmentRepository).save(appointment);
    }

    @Test
    void delete_existingAppointment_deletesSuccessfully() {
        var appointment = buildAppointment(1L, buildPatient(1L), LocalDateTime.now().plusDays(1), AppointmentStatus.SCHEDULED);
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));

        appointmentService.delete(1L);

        verify(appointmentRepository).delete(appointment);
    }

    @Test
    void search_withFilters_usesRepositorySpecificationAndPageable() {
        var scheduledAt = LocalDateTime.now().plusDays(1);
        var pageable = PageRequest.of(0, 20);
        var appointment = buildAppointment(1L, buildPatient(1L), scheduledAt, AppointmentStatus.SCHEDULED);
        var response = buildResponse(1L, 1L, scheduledAt, "SCHEDULED");

        when(appointmentRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(appointment), pageable, 1));
        when(appointmentMapper.toResponse(appointment)).thenReturn(response);

        var result = appointmentService.search(scheduledAt.toLocalDate(), 1L, "scheduled", pageable);

        assertThat(result.getContent()).containsExactly(response);
        verify(appointmentRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void search_invalidStatus_throwsBadRequestException() {
        var pageable = PageRequest.of(0, 20);

        assertThatThrownBy(() -> appointmentService.search(null, null, "waiting", pageable))
                .isInstanceOf(BadRequestException.class);
        verify(appointmentRepository, never()).findAll(any(Specification.class), eq(pageable));
    }

    private Patient buildPatient(Long id) {
        Patient patient = Patient.builder()
                .fullName("Jane Patient")
                .phone("0123456789")
                .email("jane@example.com")
                .build();
        patient.setId(id);
        return patient;
    }

    private Appointment buildAppointment(Long id, Patient patient, LocalDateTime scheduledAt, AppointmentStatus status) {
        Appointment appointment = Appointment.builder()
                .patient(patient)
                .scheduledAt(scheduledAt)
                .reason("Annual checkup")
                .status(status)
                .note("Bring lab results")
                .build();
        appointment.setId(id);
        return appointment;
    }

    private AppointmentResponse buildResponse(Long id, Long patientId, LocalDateTime scheduledAt, String status) {
        return new AppointmentResponse(id, patientId, "Jane Patient", scheduledAt, "Annual checkup", status, "Bring lab results", LocalDateTime.now());
    }
}
