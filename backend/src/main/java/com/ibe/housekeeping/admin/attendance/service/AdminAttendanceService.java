package com.ibe.housekeeping.admin.attendance.service;

import com.ibe.housekeeping.admin.attendance.dto.AdminAttendanceLogItemResponse;
import com.ibe.housekeeping.admin.attendance.dto.AdminAttendanceLogListResponse;
import com.ibe.housekeeping.admin.attendance.dto.AdminAttendanceSummaryResponse;
import com.ibe.housekeeping.attendance.repository.AttendanceRepository;
import com.ibe.housekeeping.common.enums.Role;
import com.ibe.housekeeping.entity.Attendance;
import com.ibe.housekeeping.entity.Shift;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AdminAttendanceService {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private final AttendanceRepository attendanceRepository;

    public AdminAttendanceService(AttendanceRepository attendanceRepository) {
        this.attendanceRepository = attendanceRepository;
    }

    @Transactional(readOnly = true)
    public AdminAttendanceSummaryResponse getAttendanceSummary(LocalDate fromDate, LocalDate toDate) {
        validateDateRange(fromDate, toDate);

        List<Attendance> attendances = attendanceRepository.findAllForAdminSummaryByWorkDateBetween(fromDate, toDate);
        long activeTodayCount = attendanceRepository.countByWorkDate(toDate);
        long previousCount = countPreviousPeriodAttendances(fromDate, toDate);

        double averageShiftLengthHours = roundToSingleDecimal(attendances.stream()
                .mapToInt(this::resolveWorkedMinutes)
                .filter(minutes -> minutes > 0)
                .average()
                .orElse(0.0) / 60.0);

        long lateCheckInsCount = attendances.stream()
                .filter(this::isLateCheckIn)
                .count();

        return new AdminAttendanceSummaryResponse(
                fromDate,
                toDate,
                activeTodayCount,
                calculateDeltaPercent(activeTodayCount, previousCount),
                averageShiftLengthHours,
                averageShiftLengthHours > 0 ? "Across completed shifts" : "No completed shifts in range",
                lateCheckInsCount,
                lateCheckInsCount > 0 ? "Requires review" : "All check-ins on time"
        );
    }

    @Transactional(readOnly = true)
    public AdminAttendanceLogListResponse getAttendanceLogs(
            LocalDate fromDate,
            LocalDate toDate,
            int page,
            int size,
            String search
    ) {
        validateDateRange(fromDate, toDate);
        validatePagination(page, size);

        String normalizedSearch = normalizeSearch(search);
        Page<Attendance> attendancePage = StringUtils.hasText(normalizedSearch)
                ? attendanceRepository.findAllForAdminLogsByWorkDateBetweenAndSearch(
                        fromDate,
                        toDate,
                        normalizedSearch,
                        PageRequest.of(page, size)
                )
                : attendanceRepository.findAllForAdminLogsByWorkDateBetween(
                        fromDate,
                        toDate,
                        PageRequest.of(page, size)
                );

        return new AdminAttendanceLogListResponse(
                attendancePage.getContent().stream().map(this::toLogItemResponse).toList(),
                attendancePage.getNumber(),
                attendancePage.getSize(),
                attendancePage.getTotalElements(),
                attendancePage.getTotalPages()
        );
    }

    private AdminAttendanceLogItemResponse toLogItemResponse(Attendance attendance) {
        int workedMinutes = resolveWorkedMinutes(attendance);
        boolean lateCheckIn = isLateCheckIn(attendance);
        boolean overtimeFlag = isOvertime(attendance, workedMinutes);

        return new AdminAttendanceLogItemResponse(
                attendance.getId(),
                attendance.getStaff().getId(),
                attendance.getStaff().getFullName(),
                toRoleLabel(attendance.getStaff().getUser().getRole()),
                toInitials(attendance.getStaff().getFullName()),
                attendance.getWorkDate(),
                formatTime(attendance.getClockInTime()),
                formatTime(attendance.getClockOutTime()),
                roundToSingleDecimal(workedMinutes / 60.0),
                toShiftReference(attendance.getShift()),
                lateCheckIn,
                lateCheckIn ? "Late" : "On time",
                overtimeFlag,
                determineStatusTag(attendance, lateCheckIn, overtimeFlag)
        );
    }

    private void validateDateRange(LocalDate fromDate, LocalDate toDate) {
        if (fromDate == null || toDate == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "fromDate and toDate are required.");
        }

        if (toDate.isBefore(fromDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "toDate must be on or after fromDate.");
        }
    }

    private void validatePagination(int page, int size) {
        if (page < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "page must be 0 or greater.");
        }

        if (size < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "size must be at least 1.");
        }
    }

    private long countPreviousPeriodAttendances(LocalDate fromDate, LocalDate toDate) {
        long daysInRange = Duration.between(fromDate.atStartOfDay(), toDate.plusDays(1).atStartOfDay()).toDays();
        LocalDate previousFromDate = fromDate.minusDays(daysInRange);
        LocalDate previousToDate = toDate.minusDays(daysInRange);
        return attendanceRepository.countByWorkDateBetween(previousFromDate, previousToDate);
    }

    private double calculateDeltaPercent(long currentCount, long previousCount) {
        if (previousCount <= 0) {
            return currentCount > 0 ? 100.0 : 0.0;
        }

        return roundToSingleDecimal(((currentCount - previousCount) * 100.0) / previousCount);
    }

    private int resolveWorkedMinutes(Attendance attendance) {
        if (attendance.getWorkedMinutes() != null && attendance.getWorkedMinutes() > 0) {
            return attendance.getWorkedMinutes();
        }

        if (attendance.getClockInTime() != null && attendance.getClockOutTime() != null) {
            return Math.max(0, (int) Duration.between(attendance.getClockInTime(), attendance.getClockOutTime()).toMinutes());
        }

        return 0;
    }

    private boolean isLateCheckIn(Attendance attendance) {
        return attendance.getClockInTime() != null
                && attendance.getShift() != null
                && attendance.getClockInTime().toLocalTime().isAfter(attendance.getShift().getStartTime());
    }

    private boolean isOvertime(Attendance attendance, int workedMinutes) {
        return attendance.getShift() != null
                && attendance.getShift().getDurationMinutes() != null
                && workedMinutes > attendance.getShift().getDurationMinutes();
    }

    private String determineStatusTag(Attendance attendance, boolean lateCheckIn, boolean overtimeFlag) {
        if (attendance.getClockOutTime() == null) {
            return "Active";
        }

        if (overtimeFlag) {
            return "Overtime";
        }

        if (lateCheckIn) {
            return "Late";
        }

        return "Completed";
    }

    private String normalizeSearch(String search) {
        return StringUtils.hasText(search) ? search.trim() : null;
    }

    private String formatTime(LocalDateTime value) {
        return value == null ? "--" : value.toLocalTime().format(TIME_FORMATTER);
    }

    private String toShiftReference(Shift shift) {
        if (shift == null) {
            return "Unassigned";
        }

        return shift.getShiftCode() + " - " + shift.getShiftName();
    }

    private String toInitials(String fullName) {
        if (!StringUtils.hasText(fullName)) {
            return "NA";
        }

        String[] parts = fullName.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase(Locale.ENGLISH);
        }

        return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase(Locale.ENGLISH);
    }

    private String toRoleLabel(Role role) {
        if (role == null) {
            return "Staff";
        }

        return switch (role) {
            case ADMIN -> "Administrator";
            case STAFF -> "Housekeeping Staff";
        };
    }

    private double roundToSingleDecimal(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}
