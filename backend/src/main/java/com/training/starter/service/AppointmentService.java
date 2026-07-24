package com.training.starter.service;

import com.training.starter.dto.request.CreateAppointmentRequest;
import com.training.starter.dto.response.AppointmentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface AppointmentService {

    Page<AppointmentResponse> getMyAppointments(String username, Pageable pageable);

    AppointmentResponse getMyAppointmentById(String username, Long id);

    AppointmentResponse bookAppointment(String username, CreateAppointmentRequest request);

    AppointmentResponse cancelAppointment(String username, Long id);

    Page<AppointmentResponse> getDoctorAppointments(String username, LocalDate date, Pageable pageable);
}
