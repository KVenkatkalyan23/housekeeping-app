package com.ibe.housekeeping.attendance.dto;

import com.ibe.housekeeping.common.enums.AvailabilityStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record ClockInResponse(
        UUID attendanceId,
        UUID staffId,
        UUID shiftId,
        LocalDate workDate,
        LocalDateTime clockInTime,
        Integer workedMinutes,
        AvailabilityStatus availabilityStatus
) {
}
