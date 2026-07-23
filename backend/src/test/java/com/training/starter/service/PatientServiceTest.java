package com.training.starter.service;

import com.training.starter.dto.response.PatientResponse;
import com.training.starter.entity.Patient;
import com.training.starter.exception.BadRequestException;
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
    void search_withTerm_usesRepositorySpecificationAndPageable() {
        var pageable = PageRequest.of(0, 20);
        var patient = buildPatient(1L);
        var response = buildResponse(1L);

        when(patientRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(patient), pageable, 1));
        when(patientMapper.toResponse(patient)).thenReturn(response);

        var result = patientService.search("jane", pageable);

        assertThat(result.getContent()).containsExactly(response);
        verify(patientRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void search_blankTerm_stillUsesDatabasePageableQuery() {
        var pageable = PageRequest.of(0, 20);
        when(patientRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        var result = patientService.search("   ", pageable);

        assertThat(result.getContent()).isEmpty();
        verify(patientRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void search_tooShortTerm_throwsBadRequestException() {
        var pageable = PageRequest.of(0, 20);

        assertThatThrownBy(() -> patientService.search("jo", pageable))
                .isInstanceOf(BadRequestException.class);
        verify(patientRepository, never()).findAll(any(Specification.class), eq(pageable));
    }

    private Patient buildPatient(Long id) {
        Patient patient = Patient.builder()
                .fullName("Jane Patient")
                .phone("0123456789")
                .email("jane@example.com")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .build();
        patient.setId(id);
        return patient;
    }

    private PatientResponse buildResponse(Long id) {
        return new PatientResponse(
                id,
                "Jane Patient",
                "0123456789",
                "jane@example.com",
                LocalDate.of(1990, 1, 1),
                null,
                null,
                null,
                LocalDateTime.now());
    }
}
