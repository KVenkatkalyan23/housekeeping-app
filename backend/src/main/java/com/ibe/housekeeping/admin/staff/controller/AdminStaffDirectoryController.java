package com.ibe.housekeeping.admin.staff.controller;

import com.ibe.housekeeping.admin.staff.dto.AdminStaffDirectoryResponse;
import com.ibe.housekeeping.admin.staff.service.AdminStaffDirectoryService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/staff")
@PreAuthorize("hasRole('ADMIN')")
public class AdminStaffDirectoryController {

    private final AdminStaffDirectoryService adminStaffDirectoryService;

    public AdminStaffDirectoryController(AdminStaffDirectoryService adminStaffDirectoryService) {
        this.adminStaffDirectoryService = adminStaffDirectoryService;
    }

    @GetMapping
    public AdminStaffDirectoryResponse getStaffDirectory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status
    ) {
        return adminStaffDirectoryService.getStaffDirectory(page, size, search, status);
    }
}
