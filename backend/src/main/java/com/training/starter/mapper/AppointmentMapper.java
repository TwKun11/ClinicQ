package com.training.starter.mapper;

import com.training.starter.dto.request.CreateAppointmentRequest;
import com.training.starter.dto.response.AppointmentResponse;
import com.training.starter.entity.Appointment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AppointmentMapper {

    @Mapping(target = "patientId", source = "patient.id")
    @Mapping(target = "patientName", source = "patient.fullName")
    AppointmentResponse toResponse(Appointment appointment);

    @Mapping(target = "patient", ignore = true)
    @Mapping(target = "status", ignore = true)
    Appointment toEntity(CreateAppointmentRequest request);
}
