package com.training.starter.mapper;

import com.training.starter.dto.response.AppointmentResponse;
import com.training.starter.entity.Appointment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AppointmentMapper {

    @Mapping(target = "patientId", source = "patient.id")
    @Mapping(target = "patientName", source = "patient.fullName")
    @Mapping(target = "doctorId", source = "doctor.id")
    @Mapping(target = "doctorName", source = "doctor.user.fullName")
    @Mapping(target = "specialty", source = "doctor.specialty")
    @Mapping(target = "slotId", source = "slot.id")
    AppointmentResponse toResponse(Appointment appointment);
}
