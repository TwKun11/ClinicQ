package com.training.starter.mapper;

import com.training.starter.dto.request.CreateDoctorRequest;
import com.training.starter.dto.request.UpdateDoctorRequest;
import com.training.starter.dto.response.DoctorResponse;
import com.training.starter.entity.Doctor;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface DoctorMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "fullName", source = "user.fullName")
    DoctorResponse toResponse(Doctor doctor);

    @Mapping(target = "user", ignore = true)
    Doctor toEntity(CreateDoctorRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(@MappingTarget Doctor doctor, UpdateDoctorRequest request);
}
