package com.ibe.housekeeping.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.auth.bootstrap")
public record AuthBootstrapProperties(
        boolean enabled,
        SeedUser admin,
        SeedUser staff
) {
    public record SeedUser(
            String username,
            String password
    ) {
    }
}
