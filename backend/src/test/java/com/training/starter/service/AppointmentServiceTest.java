package com.training.starter.service;

import com.training.starter.dto.request.CreateAppointmentRequest;
import com.training.starter.dto.response.AppointmentResponse;
import com.training.starter.entity.Appointment;
import com.training.starter.entity.Doctor;
import com.training.starter.entity.ScheduleSlot;
import com.training.starter.entity.User;
import com.training.starter.enums.AppointmentStatus;
import com.training.starter.enums.Role;
import com.training.starter.enums.ScheduleSlotStatus;
import com.training.starter.exception.BadRequestException;
import com.training.starter.mapper.AppointmentMapper;
import com.training.starter.repository.AppointmentRepository;
import com.training.starter.repository.ScheduleSlotRepository;
import com.training.starter.repository.UserRepository;
import com.training.starter.service.impl.AppointmentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private ScheduleSlotRepository scheduleSlotRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AppointmentMapper appointmentMapper;

    private AppointmentServiceImpl appointmentService;

    @BeforeEach
    void setUp() {
        appointmentService = new AppointmentServiceImpl(
                appointmentRepository,
                scheduleSlotRepository,
                userRepository,
                appointmentMapper);
    }

    @Test
    void bookAppointment_availableSlot_savesAppointmentAndBooksSlot() {
        var patient = user(1L, "patient", Role.USER);
        var slot = availableSlot();
        var request = new CreateAppointmentRequest(2L, 3L, "Headache", "First visit");
        var saved = appointment(patient, slot, AppointmentStatus.SCHEDULED);
        var response = response(saved);

        when(userRepository.findByUsername("patient")).thenReturn(Optional.of(patient));
        when(scheduleSlotRepository.findByIdForUpdate(3L)).thenReturn(Optional.of(slot));
        when(appointmentRepository.existsByPatientIdAndAppointmentDateAndStatusNotAndStartTimeLessThanAndEndTimeGreaterThan(
                1L, slot.getSlotDate(), AppointmentStatus.CANCELLED, slot.getEndTime(), slot.getStartTime()))
                .thenReturn(false);
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(saved);
        when(appointmentMapper.toResponse(saved)).thenReturn(response);

        AppointmentResponse result = appointmentService.bookAppointment("patient", request);

        assertThat(result.status()).isEqualTo("SCHEDULED");
        assertThat(slot.getStatus()).isEqualTo(ScheduleSlotStatus.BOOKED);
        verify(scheduleSlotRepository).save(slot);
    }

    @Test
    void bookAppointment_bookedSlot_throwsBadRequest() {
        var patient = user(1L, "patient", Role.USER);
        var slot = availableSlot();
        slot.setStatus(ScheduleSlotStatus.BOOKED);
        var request = new CreateAppointmentRequest(2L, 3L, "Headache", null);

        when(userRepository.findByUsername("patient")).thenReturn(Optional.of(patient));
        when(scheduleSlotRepository.findByIdForUpdate(3L)).thenReturn(Optional.of(slot));

        assertThatThrownBy(() -> appointmentService.bookAppointment("patient", request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("not available");
    }

    @Test
    void bookAppointment_patientOverlap_throwsBadRequest() {
        var patient = user(1L, "patient", Role.USER);
        var slot = availableSlot();
        var request = new CreateAppointmentRequest(2L, 3L, "Headache", null);

        when(userRepository.findByUsername("patient")).thenReturn(Optional.of(patient));
        when(scheduleSlotRepository.findByIdForUpdate(3L)).thenReturn(Optional.of(slot));
        when(appointmentRepository.existsByPatientIdAndAppointmentDateAndStatusNotAndStartTimeLessThanAndEndTimeGreaterThan(
                1L, slot.getSlotDate(), AppointmentStatus.CANCELLED, slot.getEndTime(), slot.getStartTime()))
                .thenReturn(true);

        assertThatThrownBy(() -> appointmentService.bookAppointment("patient", request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already has an appointment");
    }

    @Test
    void cancelAppointment_scheduledAppointment_cancelsAndReopensSlot() {
        var patient = user(1L, "patient", Role.USER);
        var slot = availableSlot();
        slot.setStatus(ScheduleSlotStatus.BOOKED);
        var appointment = appointment(patient, slot, AppointmentStatus.SCHEDULED);

        when(appointmentRepository.findByIdAndPatientPrincipal(9L, "patient")).thenReturn(Optional.of(appointment));
        when(scheduleSlotRepository.findByIdForUpdate(3L)).thenReturn(Optional.of(slot));
        when(appointmentRepository.save(appointment)).thenReturn(appointment);
        doAnswer(invocation -> response(invocation.getArgument(0))).when(appointmentMapper).toResponse(appointment);

        AppointmentResponse result = appointmentService.cancelAppointment("patient", 9L);

        assertThat(result.status()).isEqualTo("CANCELLED");
        assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.CANCELLED);
        assertThat(slot.getStatus()).isEqualTo(ScheduleSlotStatus.AVAILABLE);
    }

    private ScheduleSlot availableSlot() {
        Doctor doctor = Doctor.builder()
                .user(user(20L, "doctor", Role.DOCTOR))
                .specialty("Cardiology")
                .active(true)
                .build();
        doctor.setId(2L);
        ScheduleSlot slot = ScheduleSlot.builder()
                .doctor(doctor)
                .slotDate(LocalDate.of(2030, 1, 1))
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(9, 30))
                .status(ScheduleSlotStatus.AVAILABLE)
                .build();
        slot.setId(3L);
        return slot;
    }

    private Appointment appointment(User patient, ScheduleSlot slot, AppointmentStatus status) {
        Appointment appointment = Appointment.builder()
                .patient(patient)
                .doctor(slot.getDoctor())
                .slot(slot)
                .appointmentDate(slot.getSlotDate())
                .startTime(slot.getStartTime())
                .endTime(slot.getEndTime())
                .status(status)
                .symptoms("Headache")
                .notes("First visit")
                .build();
        appointment.setId(9L);
        return appointment;
    }

    private User user(Long id, String username, Role role) {
        User user = User.builder()
                .username(username)
                .email(username + "@example.com")
                .password("secret")
                .fullName(username)
                .role(role)
                .active(true)
                .build();
        user.setId(id);
        return user;
    }

    private AppointmentResponse response(Appointment appointment) {
        return new AppointmentResponse(
                appointment.getId(),
                appointment.getPatient().getId(),
                appointment.getPatient().getFullName(),
                appointment.getDoctor().getId(),
                appointment.getDoctor().getUser().getFullName(),
                appointment.getDoctor().getSpecialty(),
                appointment.getSlot().getId(),
                appointment.getAppointmentDate(),
                appointment.getStartTime(),
                appointment.getEndTime(),
                appointment.getStatus().name(),
                appointment.getSymptoms(),
                appointment.getNotes(),
                LocalDateTime.now());
    }
}
