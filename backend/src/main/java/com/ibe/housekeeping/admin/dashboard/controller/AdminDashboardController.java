package com.ibe.housekeeping.admin.dashboard.controller;

import com.ibe.housekeeping.admin.dashboard.dto.AdminDashboardResponse;
import com.ibe.housekeeping.admin.dashboard.service.AdminDashboardService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/dashboard")
@PreAuthorize("hasRole('ADMIN')")
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    public AdminDashboardController(AdminDashboardService adminDashboardService) {
        this.adminDashboardService = adminDashboardService;
    }

    @GetMapping
    public AdminDashboardResponse getDashboard(Authentication authentication) {
        return adminDashboardService.getDashboard(authentication.getName());
    }
}
