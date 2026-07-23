package com.training.starter.service;

import com.training.starter.dto.request.CreatePatientRequest;
import com.training.starter.dto.request.UpdatePatientRequest;
import com.training.starter.dto.response.PatientResponse;
import com.training.starter.entity.Patient;
import com.training.starter.exception.BadRequestException;
import com.training.starter.exception.DuplicateResourceException;
import com.training.starter.exception.ResourceNotFoundException;
import com.training.starter.mapper.PatientMapper;
import com.training.starter.repository.PatientRepository;
import com.training.starter.service.impl.PatientServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
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
class PatientServiceTest {

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private PatientMapper patientMapper;

    @InjectMocks
    private PatientServiceImpl patientService;

    @Test
    void create_validRequest_returnsPatientResponse() {
        var request = buildCreateRequest(
                "0123456789",
                "jane@example.com"
        );

        var patient = buildPatient(
                1L,
                "0123456789",
                "jane@example.com"
        );

        var response = buildResponse(
                1L,
                "0123456789",
                "jane@example.com"
        );

        when(patientRepository.existsByPhone("0123456789"))
                .thenReturn(false);

        when(patientRepository.existsByEmail("jane@example.com"))
                .thenReturn(false);

        when(patientMapper.toEntity(request))
                .thenReturn(patient);

        when(patientRepository.save(patient))
                .thenReturn(patient);

        when(patientMapper.toResponse(patient))
                .thenReturn(response);

        var result = patientService.create(request);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.phone()).isEqualTo("0123456789");

        verify(patientRepository).save(patient);
    }

    @Test
    void create_duplicatePhone_throwsDuplicateResourceException() {
        var request = buildCreateRequest(
                "0123456789",
                "jane@example.com"
        );

        when(patientRepository.existsByPhone("0123456789"))
                .thenReturn(true);

        assertThatThrownBy(() -> patientService.create(request))
                .isInstanceOf(DuplicateResourceException.class);

        verify(patientRepository, never()).save(any());
    }

    @Test
    void create_duplicateEmail_throwsDuplicateResourceException() {
        var request = buildCreateRequest(
                "0123456789",
                "jane@example.com"
        );

        when(patientRepository.existsByPhone("0123456789"))
                .thenReturn(false);

        when(patientRepository.existsByEmail("jane@example.com"))
                .thenReturn(true);

        assertThatThrownBy(() -> patientService.create(request))
                .isInstanceOf(DuplicateResourceException.class);

        verify(patientRepository, never()).save(any());
    }

    @Test
    void getById_found_returnsPatientResponse() {
        var patient = buildPatient(
                1L,
                "0123456789",
                "jane@example.com"
        );

        var response = buildResponse(
                1L,
                "0123456789",
                "jane@example.com"
        );

        when(patientRepository.findById(1L))
                .thenReturn(Optional.of(patient));

        when(patientMapper.toResponse(patient))
                .thenReturn(response);

        var result = patientService.getById(1L);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.email()).isEqualTo("jane@example.com");
    }

    @Test
    void getById_notFound_throwsResourceNotFoundException() {
        when(patientRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> patientService.getById(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void update_validRequest_updatesAndReturns() {
        var patient = buildPatient(
                1L,
                "0123456789",
                "jane@example.com"
        );

        var request = new UpdatePatientRequest(
                "Jane Updated",
                "0987654321",
                "updated@example.com",
                LocalDate.of(1990, 1, 1),
                "FEMALE",
                "New Address",
                "Updated note"
        );

        var response = buildResponse(
                1L,
                "0987654321",
                "updated@example.com"
        );

        when(patientRepository.findById(1L))
                .thenReturn(Optional.of(patient));

        when(patientRepository.existsByPhone("0987654321"))
                .thenReturn(false);

        when(patientRepository.existsByEmail("updated@example.com"))
                .thenReturn(false);

        when(patientRepository.save(patient))
                .thenReturn(patient);

        when(patientMapper.toResponse(patient))
                .thenReturn(response);

        var result = patientService.update(1L, request);

        assertThat(result.phone()).isEqualTo("0987654321");
        assertThat(result.email()).isEqualTo("updated@example.com");

        verify(patientMapper).updateEntity(patient, request);
        verify(patientRepository).save(patient);
    }

    @Test
    void update_duplicatePhone_throwsDuplicateResourceException() {
        var patient = buildPatient(
                1L,
                "0123456789",
                "jane@example.com"
        );

        var request = new UpdatePatientRequest(
                null,
                "0987654321",
                null,
                null,
                null,
                null,
                null
        );

        when(patientRepository.findById(1L))
                .thenReturn(Optional.of(patient));

        when(patientRepository.existsByPhone("0987654321"))
                .thenReturn(true);

        assertThatThrownBy(() -> patientService.update(1L, request))
                .isInstanceOf(DuplicateResourceException.class);

        verify(patientRepository, never()).save(any());
    }

    @Test
    void delete_existingPatient_deletesSuccessfully() {
        var patient = buildPatient(
                1L,
                "0123456789",
                "jane@example.com"
        );

        when(patientRepository.findById(1L))
                .thenReturn(Optional.of(patient));

        patientService.delete(1L);

        verify(patientRepository).delete(patient);
    }

    @Test
    void search_withTerm_usesRepositorySpecificationAndPageable() {
        var pageable = PageRequest.of(0, 20);

        var patient = buildPatient(
                1L,
                "0123456789",
                "jane@example.com"
        );

        var response = buildResponse(
                1L,
                "0123456789",
                "jane@example.com"
        );

        when(patientRepository.findAll(
                any(Specification.class),
                eq(pageable)
        )).thenReturn(
                new PageImpl<>(List.of(patient), pageable, 1)
        );

        when(patientMapper.toResponse(patient))
                .thenReturn(response);

        var result = patientService.search("jane", pageable);

        assertThat(result.getContent())
                .containsExactly(response);

        verify(patientRepository).findAll(
                any(Specification.class),
                eq(pageable)
        );
    }

    @Test
    void search_blankTerm_stillUsesDatabasePageableQuery() {
        var pageable = PageRequest.of(0, 20);

        when(patientRepository.findAll(
                any(Specification.class),
                eq(pageable)
        )).thenReturn(
                new PageImpl<>(List.of(), pageable, 0)
        );

        var result = patientService.search("   ", pageable);

        assertThat(result.getContent()).isEmpty();

        verify(patientRepository).findAll(
                any(Specification.class),
                eq(pageable)
        );
    }

    @Test
    void search_tooShortTerm_throwsBadRequestException() {
        var pageable = PageRequest.of(0, 20);

        assertThatThrownBy(
                () -> patientService.search("jo", pageable)
        ).isInstanceOf(BadRequestException.class);

        verify(patientRepository, never()).findAll(
                any(Specification.class),
                eq(pageable)
        );
    }

    private CreatePatientRequest buildCreateRequest(
            String phone,
            String email
    ) {
        return new CreatePatientRequest(
                "Jane Patient",
                phone,
                email,
                LocalDate.of(1990, 1, 1),
                "FEMALE",
                "123 Street",
                "No allergies"
        );
    }

    private Patient buildPatient(
            Long id,
            String phone,
            String email
    ) {
        Patient patient = Patient.builder()
                .fullName("Jane Patient")
                .phone(phone)
                .email(email)
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .gender("FEMALE")
                .address("123 Street")
                .medicalNote("No allergies")
                .build();

        patient.setId(id);

        return patient;
    }

    private PatientResponse buildResponse(
            Long id,
            String phone,
            String email
    ) {
        return new PatientResponse(
                id,
                "Jane Patient",
                phone,
                email,
                LocalDate.of(1990, 1, 1),
                "FEMALE",
                "123 Street",
                "No allergies",
                LocalDateTime.now()
        );
    }
}