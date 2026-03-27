package com.ibe.housekeeping.leave.controller;

import com.ibe.housekeeping.leave.dto.AdminLeavesResponse;
import com.ibe.housekeeping.leave.dto.ApplyLeaveRequest;
import com.ibe.housekeeping.leave.dto.LeaveListItemResponse;
import com.ibe.housekeeping.leave.dto.MyLeavesResponse;
import com.ibe.housekeeping.leave.service.LeaveService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/leave")
public class LeaveController {

    private final LeaveService leaveService;

    public LeaveController(LeaveService leaveService) {
        this.leaveService = leaveService;
    }

    @PostMapping("/apply")
    @PreAuthorize("hasRole('STAFF')")
    @ResponseStatus(HttpStatus.CREATED)
    public LeaveListItemResponse applyLeave(
            Authentication authentication,
            @Valid @RequestBody ApplyLeaveRequest request
    ) {
        return leaveService.applyLeave(authentication.getName(), request);
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('STAFF')")
    public MyLeavesResponse getMyLeaves(
            Authentication authentication,
            @RequestParam String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        return leaveService.getMyLeaves(authentication.getName(), userId, page, size);
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public AdminLeavesResponse getAdminLeaves(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return leaveService.getAdminLeaves(authentication.getName(), page, size);
    }
}
