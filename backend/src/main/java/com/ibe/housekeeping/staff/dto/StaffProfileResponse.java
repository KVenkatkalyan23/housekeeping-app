package com.ibe.housekeeping.staff.dto;

import com.ibe.housekeeping.common.enums.AvailabilityStatus;
import java.util.UUID;

public record StaffProfileResponse(
        UUID staffId,
        UUID userId,
        String fullName,
        String displayName,
        String email,
        String phone,
        String preferredShift,
        String assignedWing,
        AvailabilityStatus availabilityStatus
) {
}
