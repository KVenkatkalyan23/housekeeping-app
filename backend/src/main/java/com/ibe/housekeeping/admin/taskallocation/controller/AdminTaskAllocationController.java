package com.ibe.housekeeping.admin.taskallocation.controller;

import com.ibe.housekeeping.admin.taskallocation.dto.AdminAllocatedTaskListResponse;
import com.ibe.housekeeping.admin.taskallocation.dto.AdminTaskAllocationSummaryResponse;
import com.ibe.housekeeping.admin.taskallocation.service.AdminTaskAllocationService;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/tasks/allocation")
@PreAuthorize("hasRole('ADMIN')")
public class AdminTaskAllocationController {

    private final AdminTaskAllocationService adminTaskAllocationService;

    public AdminTaskAllocationController(AdminTaskAllocationService adminTaskAllocationService) {
        this.adminTaskAllocationService = adminTaskAllocationService;
    }

    @GetMapping("/summary")
    public AdminTaskAllocationSummaryResponse getAllocationSummary(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate taskDate
    ) {
        return adminTaskAllocationService.getAllocationSummary(taskDate);
    }

    @GetMapping
    public AdminAllocatedTaskListResponse getAllocatedTasks(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate taskDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String taskType,
            @RequestParam(required = false) String status
    ) {
        return adminTaskAllocationService.getAllocatedTasks(taskDate, page, size, search, taskType, status);
    }
}
