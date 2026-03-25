package com.ibe.housekeeping.leave.controller;

import com.ibe.housekeeping.leave.dto.LeaveHistoryItemResponse;
import com.ibe.housekeeping.leave.service.LeaveHistoryService;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/leave")
@PreAuthorize("hasRole('STAFF')")
public class LeaveController {

    private final LeaveHistoryService leaveHistoryService;

    public LeaveController(LeaveHistoryService leaveHistoryService) {
        this.leaveHistoryService = leaveHistoryService;
    }

    @GetMapping("/history")
    public List<LeaveHistoryItemResponse> getLeaveHistory(Authentication authentication) {
        return leaveHistoryService.getLeaveHistory(authentication.getName());
    }
}
