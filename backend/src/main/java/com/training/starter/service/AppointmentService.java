package com.training.starter.service;

import com.training.starter.dto.request.CreateAppointmentRequest;
import com.training.starter.dto.request.UpdateAppointmentRequest;
import com.training.starter.dto.response.AppointmentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AppointmentService {

    Page<AppointmentResponse> getAll(Pageable pageable);

    AppointmentResponse getById(Long id);

    AppointmentResponse create(CreateAppointmentRequest request);

    AppointmentResponse update(Long id, UpdateAppointmentRequest request);

    void delete(Long id);
}
