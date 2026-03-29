package com.ibe.housekeeping.admin.taskreassignment.controller;

import com.ibe.housekeeping.admin.taskreassignment.dto.ManualTaskReassignmentRequest;
import com.ibe.housekeeping.admin.taskreassignment.dto.ManualTaskReassignmentResponse;
import com.ibe.housekeeping.admin.taskreassignment.dto.ReassignmentCandidateItemResponse;
import com.ibe.housekeeping.admin.taskreassignment.service.AdminTaskReassignmentService;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/tasks/reassign")
@PreAuthorize("hasRole('ADMIN')")
public class AdminTaskReassignmentController {

    private final AdminTaskReassignmentService adminTaskReassignmentService;

    public AdminTaskReassignmentController(AdminTaskReassignmentService adminTaskReassignmentService) {
        this.adminTaskReassignmentService = adminTaskReassignmentService;
    }

    @PostMapping
    public ManualTaskReassignmentResponse reassignTask(
            @Valid @RequestBody ManualTaskReassignmentRequest request,
            Principal principal
    ) {
        return adminTaskReassignmentService.reassignTask(
                request.taskId(),
                request.targetStaffId(),
                principal.getName()
        );
    }

    @GetMapping("/candidates")
    public List<ReassignmentCandidateItemResponse> getCandidates(@RequestParam UUID taskId) {
        return adminTaskReassignmentService.getCandidates(taskId);
    }
}
