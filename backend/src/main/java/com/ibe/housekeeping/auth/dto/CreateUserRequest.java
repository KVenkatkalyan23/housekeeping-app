package com.ibe.housekeeping.auth.dto;

import com.ibe.housekeeping.common.enums.Role;
import jakarta.validation.constraints.NotBlank;

public record CreateUserRequest(
        @NotBlank(message = "username is required")
        String username,
        @NotBlank(message = "password is required")
        String password,
        Role role
) {
}
