package com.ibe.housekeeping.staff.service;

import com.ibe.housekeeping.auth.repository.UserRepository;
import com.ibe.housekeeping.entity.Shift;
import com.ibe.housekeeping.entity.StaffProfile;
import com.ibe.housekeeping.entity.User;
import com.ibe.housekeeping.staff.dto.StaffProfileResponse;
import com.ibe.housekeeping.staff.repository.StaffProfileRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class StaffProfileService {

    private final StaffProfileRepository staffProfileRepository;
    private final UserRepository userRepository;

    public StaffProfileService(
            StaffProfileRepository staffProfileRepository,
            UserRepository userRepository
    ) {
        this.staffProfileRepository = staffProfileRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public StaffProfileResponse getCurrentStaffProfile(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required"));

        StaffProfile staffProfile = staffProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Staff profile not found for this user."));

        return new StaffProfileResponse(
                staffProfile.getId(),
                user.getId(),
                staffProfile.getFullName(),
                staffProfile.getFullName(),
                staffProfile.getEmail(),
                staffProfile.getPhone(),
                formatShift(staffProfile.getPreferredShift()),
                "North Suites, Floor 4-6",
                staffProfile.getAvailabilityStatus()
        );
    }

    private String formatShift(Shift shift) {
        if (shift == null) {
            return "No Shift Assigned";
        }

        if (shift.getShiftName() != null && !shift.getShiftName().isBlank()) {
            return shift.getShiftName();
        }

        if (shift.getShiftCode() != null && !shift.getShiftCode().isBlank()) {
            return shift.getShiftCode();
        }

        return "Shift Assigned";
    }
}
