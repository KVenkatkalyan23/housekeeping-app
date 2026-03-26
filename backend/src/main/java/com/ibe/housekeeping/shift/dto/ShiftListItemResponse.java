package com.ibe.housekeeping.shift.dto;

import java.time.LocalTime;
import java.util.UUID;

public record ShiftListItemResponse(
        UUID id,
        String shiftCode,
        String shiftName,
        LocalTime startTime,
        LocalTime endTime,
        Integer durationMinutes
) {
}
