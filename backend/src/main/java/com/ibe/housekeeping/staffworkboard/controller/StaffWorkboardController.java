package com.ibe.housekeeping.staffworkboard.controller;

import com.ibe.housekeeping.staffworkboard.dto.MarkTaskCompleteResponse;
import com.ibe.housekeeping.staffworkboard.dto.MyAssignedTasksResponse;
import com.ibe.housekeeping.staffworkboard.dto.WorkloadSummaryResponse;
import com.ibe.housekeeping.staffworkboard.service.StaffWorkboardService;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/staff/tasks")
@PreAuthorize("hasRole('STAFF')")
public class StaffWorkboardController {

    private final StaffWorkboardService staffWorkboardService;

    public StaffWorkboardController(StaffWorkboardService staffWorkboardService) {
        this.staffWorkboardService = staffWorkboardService;
    }

    @GetMapping("/today")
    public MyAssignedTasksResponse getTodayTasks(Authentication authentication) {
        return staffWorkboardService.getMyAssignedTasks(authentication.getName(), LocalDate.now());
    }

    @GetMapping("/workload")
    public WorkloadSummaryResponse getTodayWorkload(Authentication authentication) {
        return staffWorkboardService.getMyWorkload(authentication.getName(), LocalDate.now());
    }

    @PostMapping("/{taskId}/complete")
    public MarkTaskCompleteResponse markTaskComplete(
            Authentication authentication,
            @PathVariable UUID taskId
    ) {
        return staffWorkboardService.markTaskComplete(authentication.getName(), taskId);
    }
}
