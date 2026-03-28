package com.ibe.housekeeping.admin.staff.dto;

import java.util.UUID;

public record AdminStaffDirectoryItemResponse(
        UUID staffId,
        String staffCode,
        String fullName,
        String email,
        String phone,
        String status,
        UUID userId,
        String avatarUrl
) {
}
