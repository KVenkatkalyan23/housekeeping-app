package com.ibe.housekeeping.admin.logs.controller;

import com.ibe.housekeeping.admin.logs.dto.AdminAuditLogsResponse;
import com.ibe.housekeeping.admin.logs.service.AdminAuditLogsService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/logs")
@PreAuthorize("hasRole('ADMIN')")
public class AdminAuditLogsController {

    private final AdminAuditLogsService adminAuditLogsService;

    public AdminAuditLogsController(AdminAuditLogsService adminAuditLogsService) {
        this.adminAuditLogsService = adminAuditLogsService;
    }

    @GetMapping
    public AdminAuditLogsResponse getLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String eventCode,
            @RequestParam(required = false) String severity
    ) {
        return adminAuditLogsService.getLogs(page, size, category, eventCode, severity);
    }
}
