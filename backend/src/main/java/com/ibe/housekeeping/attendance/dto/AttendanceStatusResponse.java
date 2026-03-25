package com.ibe.housekeeping.attendance.dto;

import com.ibe.housekeeping.common.enums.AvailabilityStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

public record AttendanceStatusResponse(
        boolean onDuty,
        UUID attendanceId,
        UUID staffId,
        UUID shiftId,
        String shiftCode,
        String shiftName,
        LocalTime shiftStartTime,
        LocalTime shiftEndTime,
        LocalDate workDate,
        LocalDateTime clockInTime,
        LocalDateTime clockOutTime,
        Integer workedMinutes,
        AvailabilityStatus availabilityStatus
) {
}
