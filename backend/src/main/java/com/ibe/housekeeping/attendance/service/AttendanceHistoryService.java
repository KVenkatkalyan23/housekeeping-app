package com.ibe.housekeeping.attendance.service;

import com.ibe.housekeeping.attendance.dto.WeeklyAttendanceHistoryResponse;
import com.ibe.housekeeping.attendance.dto.WeeklyAttendanceLogItemResponse;
import com.ibe.housekeeping.attendance.dto.WeeklyAttendanceSummaryResponse;
import com.ibe.housekeeping.attendance.repository.AttendanceRepository;
import com.ibe.housekeeping.auth.repository.UserRepository;
import com.ibe.housekeeping.entity.Attendance;
import com.ibe.housekeeping.entity.Shift;
import com.ibe.housekeeping.entity.StaffProfile;
import com.ibe.housekeeping.entity.User;
import com.ibe.housekeeping.staff.repository.StaffProfileRepository;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AttendanceHistoryService {

    private static final DateTimeFormatter DAY_LABEL_FORMATTER = DateTimeFormatter.ofPattern("EEEE, MMM d");

    private final AttendanceRepository attendanceRepository;
    private final StaffProfileRepository staffProfileRepository;
    private final UserRepository userRepository;

    public AttendanceHistoryService(
            AttendanceRepository attendanceRepository,
            StaffProfileRepository staffProfileRepository,
            UserRepository userRepository
    ) {
        this.attendanceRepository = attendanceRepository;
        this.staffProfileRepository = staffProfileRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public WeeklyAttendanceHistoryResponse getWeeklyHistory(String username, LocalDate requestedWeekStart) {
        StaffProfile staffProfile = loadStaffProfile(username);
        LocalDate weekStart = resolveWeekStart(requestedWeekStart);
        LocalDate weekEnd = weekStart.plusDays(6);

        List<Attendance> attendances = attendanceRepository
                .findAllByStaffIdAndWorkDateBetweenOrderByWorkDateAscClockInTimeAsc(staffProfile.getId(), weekStart, weekEnd);

        List<WeeklyAttendanceLogItemResponse> logs = attendances.stream()
                .map(this::toLogItem)
                .toList();

        int totalWorkedMinutes = attendances.stream()
                .map(Attendance::getWorkedMinutes)
                .filter(minutes -> minutes != null && minutes > 0)
                .mapToInt(Integer::intValue)
                .sum();

        int overtimeMinutes = attendances.stream()
                .mapToInt(this::calculateOvertimeMinutes)
                .sum();

        int totalBreakMinutes = Math.max(attendances.size() * 45, 0);

        return new WeeklyAttendanceHistoryResponse(
                weekStart,
                weekEnd,
                new WeeklyAttendanceSummaryResponse(totalWorkedMinutes, overtimeMinutes, totalBreakMinutes),
                logs
        );
    }

    private StaffProfile loadStaffProfile(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required"));

        return staffProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Staff profile not found for this user."));
    }

    private LocalDate resolveWeekStart(LocalDate requestedWeekStart) {
        LocalDate anchorDate = requestedWeekStart != null ? requestedWeekStart : LocalDate.now();
        return anchorDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }

    private WeeklyAttendanceLogItemResponse toLogItem(Attendance attendance) {
        return new WeeklyAttendanceLogItemResponse(
                attendance.getWorkDate(),
                attendance.getWorkDate().format(DAY_LABEL_FORMATTER),
                attendance.getClockInTime(),
                attendance.getClockOutTime(),
                attendance.getWorkedMinutes(),
                deriveStatusLabel(attendance),
                deriveStatusType(attendance)
        );
    }

    private int calculateOvertimeMinutes(Attendance attendance) {
        Shift shift = attendance.getShift();

        if (shift == null || attendance.getWorkedMinutes() == null) {
            return 0;
        }

        return Math.max(attendance.getWorkedMinutes() - shift.getDurationMinutes(), 0);
    }

    private String deriveStatusLabel(Attendance attendance) {
        Shift shift = attendance.getShift();

        if (shift != null && attendance.getClockInTime() != null) {
            LocalTime shiftStart = shift.getStartTime();
            LocalTime clockInTime = attendance.getClockInTime().toLocalTime();

            if (clockInTime.isAfter(shiftStart.plusMinutes(5))) {
                long lateMinutes = java.time.Duration.between(shiftStart, clockInTime).toMinutes();
                return "LATE IN (" + lateMinutes + "M)";
            }
        }

        if (calculateOvertimeMinutes(attendance) > 0) {
            return "OVERTIME";
        }

        return "ON TIME";
    }

    private String deriveStatusType(Attendance attendance) {
        String statusLabel = deriveStatusLabel(attendance);

        if (statusLabel.startsWith("LATE IN")) {
            return "LATE";
        }

        if ("OVERTIME".equals(statusLabel)) {
            return "OVERTIME";
        }

        return "ON_TIME";
    }
}
