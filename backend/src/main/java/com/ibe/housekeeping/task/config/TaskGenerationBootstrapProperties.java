package com.ibe.housekeeping.task.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.task-generation.bootstrap")
public record TaskGenerationBootstrapProperties(
        boolean enabled
) {
}
