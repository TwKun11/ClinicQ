package com.training.starter.service;

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
import com.training.starter.service.impl.DoctorServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DoctorServiceTest {

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private DoctorMapper doctorMapper;

    @InjectMocks
    private DoctorServiceImpl doctorService;

    @Test
    void create_validRequest_returnsDoctorResponse() {
        var request = new CreateDoctorRequest(10L, "Cardiology", "A101", 24, true);
        var user = buildUser(10L);
        var doctor = buildDoctor(1L, user);
        var response = buildResponse(1L, 10L, "Cardiology", true);

        when(doctorRepository.existsByUserId(10L)).thenReturn(false);
        when(userRepository.findById(10L)).thenReturn(Optional.of(user));
        when(doctorMapper.toEntity(request)).thenReturn(doctor);
        when(doctorRepository.save(doctor)).thenReturn(doctor);
        when(doctorMapper.toResponse(doctor)).thenReturn(response);

        var result = doctorService.create(request);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.userId()).isEqualTo(10L);
        assertThat(result.specialty()).isEqualTo("Cardiology");
        verify(doctorRepository).save(doctor);
    }

    @Test
    void create_duplicateUser_throwsDuplicateResourceException() {
        var request = new CreateDoctorRequest(10L, "Cardiology", "A101", 24, true);

        when(doctorRepository.existsByUserId(10L)).thenReturn(true);

        assertThatThrownBy(() -> doctorService.create(request))
                .isInstanceOf(DuplicateResourceException.class);

        verify(doctorRepository, never()).save(any());
    }

    @Test
    void getById_found_returnsDoctorResponse() {
        var user = buildUser(10L);
        var doctor = buildDoctor(1L, user);
        var response = buildResponse(1L, 10L, "Cardiology", true);

        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
        when(doctorMapper.toResponse(doctor)).thenReturn(response);

        var result = doctorService.getById(1L);

        assertThat(result.fullName()).isEqualTo("Dr. Lisa Martin");
        assertThat(result.roomNumber()).isEqualTo("A101");
    }

    @Test
    void getById_notFound_throwsResourceNotFoundException() {
        when(doctorRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> doctorService.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void update_validRequest_updatesDoctorInfo() {
        var doctor = buildDoctor(1L, buildUser(10L));
        var request = new UpdateDoctorRequest("Pediatrics", "B202", 18, false);
        var response = buildResponse(1L, 10L, "Pediatrics", false);

        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
        when(doctorRepository.save(doctor)).thenReturn(doctor);
        when(doctorMapper.toResponse(doctor)).thenReturn(response);

        var result = doctorService.update(1L, request);

        assertThat(result.specialty()).isEqualTo("Pediatrics");
        assertThat(result.active()).isFalse();
        verify(doctorMapper).updateEntity(doctor, request);
    }

    @Test
    void getAll_withSpecialty_usesSpecialtyFilter() {
        var pageable = PageRequest.of(0, 20);
        var doctor = buildDoctor(1L, buildUser(10L));
        var response = buildResponse(1L, 10L, "Cardiology", true);

        when(doctorRepository.findBySpecialtyContainingIgnoreCase("cardio", pageable))
                .thenReturn(new PageImpl<>(List.of(doctor), pageable, 1));
        when(doctorMapper.toResponse(doctor)).thenReturn(response);

        var result = doctorService.getAll("cardio", pageable);

        assertThat(result.getContent()).containsExactly(response);
    }

    @Test
    void getAll_tooShortSpecialty_throwsBadRequestException() {
        var pageable = PageRequest.of(0, 20);

        assertThatThrownBy(() -> doctorService.getAll("c", pageable))
                .isInstanceOf(BadRequestException.class);

        verify(doctorRepository, never()).findAll(pageable);
    }

    private User buildUser(Long id) {
        User user = User.builder()
                .username("doctor.lisa")
                .email("lisa@example.com")
                .fullName("Dr. Lisa Martin")
                .password("encoded")
                .build();
        user.setId(id);
        return user;
    }

    private Doctor buildDoctor(Long id, User user) {
        Doctor doctor = Doctor.builder()
                .user(user)
                .specialty("Cardiology")
                .roomNumber("A101")
                .maxPatientsPerDay(24)
                .active(true)
                .build();
        doctor.setId(id);
        return doctor;
    }

    private DoctorResponse buildResponse(Long id, Long userId, String specialty, boolean active) {
        return new DoctorResponse(
                id,
                userId,
                "Dr. Lisa Martin",
                specialty,
                "A101",
                24,
                active,
                LocalDateTime.now()
        );
    }
}
