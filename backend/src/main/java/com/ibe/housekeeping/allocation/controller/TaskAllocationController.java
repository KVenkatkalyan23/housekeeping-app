package com.ibe.housekeeping.allocation.controller;

import com.ibe.housekeeping.allocation.dto.RunAllocationRequest;
import com.ibe.housekeeping.allocation.dto.RunAllocationResponse;
import com.ibe.housekeeping.allocation.dto.TaskAssignmentItemResponse;
import com.ibe.housekeeping.allocation.service.TaskAllocationService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/allocation")
//@PreAuthorize("hasRole('ADMIN')")
public class TaskAllocationController {

    private final TaskAllocationService taskAllocationService;

    public TaskAllocationController(TaskAllocationService taskAllocationService) {
        this.taskAllocationService = taskAllocationService;
    }

    @PostMapping("/run")
    public RunAllocationResponse runAllocation(@Valid @RequestBody RunAllocationRequest request) {
        return taskAllocationService.runAllocation(request.taskDate());
    }

    @GetMapping
    public RunAllocationResponse getAllocation(@RequestParam LocalDate taskDate) {
        return taskAllocationService.getAllocation(taskDate);
    }

    @GetMapping("/assignments")
    public List<TaskAssignmentItemResponse> getTaskAssignments(@RequestParam LocalDate taskDate) {
        return taskAllocationService.getTaskAssignments(taskDate);
    }
}
