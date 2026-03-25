package com.ibe.housekeeping.staff.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.staff.bootstrap")
public record StaffBootstrapProperties(
        boolean enabled,
        String defaultPassword,
        int count
) {
}
