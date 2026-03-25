package com.ibe.housekeeping.auth.dto;

import com.ibe.housekeeping.common.enums.Role;
import java.util.UUID;

public record LoginResponse(
        String accessToken,
        String tokenType,
        UUID userId,
        String username,
        Role role
) {
}
