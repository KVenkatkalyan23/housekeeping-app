package com.ibe.housekeeping.task.controller;

import com.ibe.housekeeping.common.enums.TaskStatus;
import com.ibe.housekeeping.task.dto.CleaningTaskListItemResponse;
import com.ibe.housekeeping.task.dto.GenerateTasksRequest;
import com.ibe.housekeeping.task.dto.GenerateTasksResponse;
import com.ibe.housekeeping.task.service.CleaningTaskService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tasks")
public class CleaningTaskController {

    private final CleaningTaskService cleaningTaskService;

    public CleaningTaskController(CleaningTaskService cleaningTaskService) {
        this.cleaningTaskService = cleaningTaskService;
    }

    @PostMapping("/generate")
    public GenerateTasksResponse generateTasks(@Valid @RequestBody GenerateTasksRequest request) {
        return cleaningTaskService.generateTasks(request.taskDate());
    }

    @GetMapping
    public List<CleaningTaskListItemResponse> getTasks(
            @RequestParam LocalDate taskDate,
            @RequestParam(required = false) TaskStatus taskStatus
    ) {
        return cleaningTaskService.getTasksByDate(taskDate, taskStatus);
    }
}
