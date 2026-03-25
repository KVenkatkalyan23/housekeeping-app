package com.ibe.housekeeping.attendance.controller;

import com.ibe.housekeeping.attendance.dto.AttendanceStatusResponse;
import com.ibe.housekeeping.attendance.dto.ClockInResponse;
import com.ibe.housekeeping.attendance.dto.ClockOutResponse;
import com.ibe.housekeeping.attendance.dto.WeeklyAttendanceHistoryResponse;
import com.ibe.housekeeping.attendance.service.AttendanceHistoryService;
import com.ibe.housekeeping.attendance.service.AttendanceService;
import java.time.LocalDate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/attendance")
@PreAuthorize("hasRole('STAFF')")
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final AttendanceHistoryService attendanceHistoryService;

    public AttendanceController(
            AttendanceService attendanceService,
            AttendanceHistoryService attendanceHistoryService
    ) {
        this.attendanceService = attendanceService;
        this.attendanceHistoryService = attendanceHistoryService;
    }

    @GetMapping("/current")
    public AttendanceStatusResponse getCurrentAttendance(Authentication authentication) {
        return attendanceService.getCurrentAttendance(authentication.getName());
    }

    @PostMapping("/clock-in")
    public ClockInResponse clockIn(Authentication authentication) {
        return attendanceService.clockIn(authentication.getName());
    }

    @PostMapping("/clock-out")
    public ClockOutResponse clockOut(Authentication authentication) {
        return attendanceService.clockOut(authentication.getName());
    }

    @GetMapping("/weekly-history")
    public WeeklyAttendanceHistoryResponse getWeeklyHistory(
            Authentication authentication,
            @RequestParam(required = false) LocalDate weekStart,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "7") int size
    ) {
        return attendanceHistoryService.getWeeklyHistory(authentication.getName(), weekStart, page, size);
    }
}
