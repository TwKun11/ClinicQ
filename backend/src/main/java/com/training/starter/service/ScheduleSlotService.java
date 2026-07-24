package com.training.starter.service;

import com.training.starter.dto.request.GenerateScheduleSlotsRequest;
import com.training.starter.dto.response.ScheduleSlotResponse;

import java.time.LocalDate;
import java.util.List;

public interface ScheduleSlotService {

    List<ScheduleSlotResponse> getAvailableSlots(Long doctorId, LocalDate date);

    List<ScheduleSlotResponse> generateSlots(Long doctorId, GenerateScheduleSlotsRequest request);

    ScheduleSlotResponse blockSlot(Long slotId);
}
