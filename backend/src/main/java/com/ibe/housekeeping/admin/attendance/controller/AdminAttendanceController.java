package com.ibe.housekeeping.admin.attendance.controller;

import com.ibe.housekeeping.admin.attendance.dto.AdminAttendanceLogListResponse;
import com.ibe.housekeeping.admin.attendance.dto.AdminAttendanceSummaryResponse;
import com.ibe.housekeeping.admin.attendance.service.AdminAttendanceService;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/attendance")
@PreAuthorize("hasRole('ADMIN')")
public class AdminAttendanceController {

    private final AdminAttendanceService adminAttendanceService;

    public AdminAttendanceController(AdminAttendanceService adminAttendanceService) {
        this.adminAttendanceService = adminAttendanceService;
    }

    @GetMapping("/summary")
    public AdminAttendanceSummaryResponse getSummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate
    ) {
        return adminAttendanceService.getAttendanceSummary(fromDate, toDate);
    }

    @GetMapping("/logs")
    public AdminAttendanceLogListResponse getLogs(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search
    ) {
        return adminAttendanceService.getAttendanceLogs(fromDate, toDate, page, size, search);
    }
}
