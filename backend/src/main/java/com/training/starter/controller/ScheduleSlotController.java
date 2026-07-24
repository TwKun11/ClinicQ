package com.training.starter.controller;

import com.training.starter.common.ApiResponse;
import com.training.starter.dto.request.GenerateScheduleSlotsRequest;
import com.training.starter.dto.response.ScheduleSlotResponse;
import com.training.starter.service.ScheduleSlotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Schedule slots", description = "Doctor schedule slot operations")
public class ScheduleSlotController {

    private final ScheduleSlotService scheduleSlotService;

    @GetMapping("/api/v1/doctors/{doctorId}/slots")
    @Operation(summary = "List available slots for a doctor")
    public ApiResponse<List<ScheduleSlotResponse>> getAvailableSlots(
            @PathVariable Long doctorId,
            @RequestParam(required = false) LocalDate date) {
        return ApiResponse.success(scheduleSlotService.getAvailableSlots(doctorId, date));
    }

    @PostMapping("/api/v1/admin/doctors/{doctorId}/slots")
    @Operation(summary = "Generate schedule slots for a doctor")
    public ApiResponse<List<ScheduleSlotResponse>> generateSlots(
            @PathVariable Long doctorId,
            @Valid @RequestBody GenerateScheduleSlotsRequest request) {
        return ApiResponse.success("Schedule slots generated", scheduleSlotService.generateSlots(doctorId, request));
    }

    @PutMapping("/api/v1/admin/slots/{slotId}/block")
    @Operation(summary = "Block an available schedule slot")
    public ApiResponse<ScheduleSlotResponse> blockSlot(@PathVariable Long slotId) {
        return ApiResponse.success("Schedule slot blocked", scheduleSlotService.blockSlot(slotId));
    }
}
