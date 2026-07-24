package com.training.starter.service.impl;

import com.training.starter.dto.request.GenerateScheduleSlotsRequest;
import com.training.starter.dto.response.ScheduleSlotResponse;
import com.training.starter.entity.Doctor;
import com.training.starter.entity.ScheduleSlot;
import com.training.starter.enums.ScheduleSlotStatus;
import com.training.starter.exception.BadRequestException;
import com.training.starter.exception.ResourceNotFoundException;
import com.training.starter.repository.DoctorRepository;
import com.training.starter.repository.ScheduleSlotRepository;
import com.training.starter.service.ScheduleSlotService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ScheduleSlotServiceImpl implements ScheduleSlotService {

    private final DoctorRepository doctorRepository;
    private final ScheduleSlotRepository scheduleSlotRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ScheduleSlotResponse> getAvailableSlots(Long doctorId, LocalDate date) {
        LocalDate slotDate = date == null ? LocalDate.now() : date;
        return scheduleSlotRepository.findByDoctorIdAndSlotDateAndStatusOrderByStartTime(
                        doctorId,
                        slotDate,
                        ScheduleSlotStatus.AVAILABLE)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public List<ScheduleSlotResponse> generateSlots(Long doctorId, GenerateScheduleSlotsRequest request) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", doctorId));
        validateRange(request);

        List<ScheduleSlotResponse> created = new ArrayList<>();
        for (LocalDate date = request.startDate(); !date.isAfter(request.endDate()); date = date.plusDays(1)) {
            for (LocalTime start = request.startTime(); start.plusMinutes(request.slotMinutes()).compareTo(request.endTime()) <= 0;
                 start = start.plusMinutes(request.slotMinutes())) {
                if (scheduleSlotRepository.existsByDoctorIdAndSlotDateAndStartTime(doctorId, date, start)) {
                    continue;
                }
                ScheduleSlot slot = ScheduleSlot.builder()
                        .doctor(doctor)
                        .slotDate(date)
                        .startTime(start)
                        .endTime(start.plusMinutes(request.slotMinutes()))
                        .status(ScheduleSlotStatus.AVAILABLE)
                        .build();
                created.add(toResponse(scheduleSlotRepository.save(slot)));
            }
        }
        return created;
    }

    @Override
    @Transactional
    public ScheduleSlotResponse blockSlot(Long slotId) {
        ScheduleSlot slot = scheduleSlotRepository.findByIdForUpdate(slotId)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule slot", slotId));
        if (slot.getStatus() == ScheduleSlotStatus.BOOKED) {
            throw new BadRequestException("Booked schedule slot cannot be blocked");
        }
        slot.setStatus(ScheduleSlotStatus.BLOCKED);
        return toResponse(scheduleSlotRepository.save(slot));
    }

    private void validateRange(GenerateScheduleSlotsRequest request) {
        if (request.endDate().isBefore(request.startDate())) {
            throw new BadRequestException("End date must be after start date");
        }
        if (!request.endTime().isAfter(request.startTime())) {
            throw new BadRequestException("End time must be after start time");
        }
    }

    private ScheduleSlotResponse toResponse(ScheduleSlot slot) {
        return new ScheduleSlotResponse(
                slot.getId(),
                slot.getDoctor().getId(),
                slot.getDoctor().getUser().getFullName(),
                slot.getSlotDate(),
                slot.getStartTime(),
                slot.getEndTime(),
                slot.getStatus().name());
    }
}
