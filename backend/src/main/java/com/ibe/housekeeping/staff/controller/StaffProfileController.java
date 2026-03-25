package com.ibe.housekeeping.staff.controller;

import com.ibe.housekeeping.staff.dto.StaffProfileResponse;
import com.ibe.housekeeping.staff.service.StaffProfileService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/staff")
@PreAuthorize("hasRole('STAFF')")
public class StaffProfileController {

    private final StaffProfileService staffProfileService;

    public StaffProfileController(StaffProfileService staffProfileService) {
        this.staffProfileService = staffProfileService;
    }

    @GetMapping("/profile")
    public StaffProfileResponse getCurrentProfile(Authentication authentication) {
        return staffProfileService.getCurrentStaffProfile(authentication.getName());
    }
}
