package com.training.starter.mapper;

import com.training.starter.dto.request.CreatePatientRequest;
import com.training.starter.dto.request.UpdatePatientRequest;
import com.training.starter.dto.response.PatientResponse;
import com.training.starter.entity.Patient;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface PatientMapper {

    PatientResponse toResponse(Patient patient);

    Patient toEntity(CreatePatientRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(@MappingTarget Patient patient, UpdatePatientRequest request);
}
