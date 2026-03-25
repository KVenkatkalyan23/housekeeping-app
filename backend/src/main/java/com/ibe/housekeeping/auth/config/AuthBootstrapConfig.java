package com.ibe.housekeeping.auth.config;

import com.ibe.housekeeping.auth.dto.CreateUserRequest;
import com.ibe.housekeeping.auth.repository.UserRepository;
import com.ibe.housekeeping.auth.service.AuthService;
import com.ibe.housekeeping.common.enums.Role;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(AuthBootstrapProperties.class)
public class AuthBootstrapConfig {

    @Bean
    public CommandLineRunner bootstrapAdminUser(
            AuthBootstrapProperties properties,
            UserRepository userRepository,
            AuthService authService
    ) {
        return args -> {
            if (!properties.enabled()) {
                return;
            }

            seedUser(properties.admin(), Role.ADMIN, userRepository, authService);
            seedUser(properties.staff(), Role.STAFF, userRepository, authService);
        };
    }

    private void seedUser(
            AuthBootstrapProperties.SeedUser seedUser,
            Role role,
            UserRepository userRepository,
            AuthService authService
    ) {
        if (seedUser == null) {
            return;
        }

        if (seedUser.username() == null || seedUser.username().isBlank()) {
            return;
        }

        if (seedUser.password() == null || seedUser.password().isBlank()) {
            return;
        }

        if (userRepository.existsByUsername(seedUser.username())) {
            return;
        }

        authService.createUser(new CreateUserRequest(
                seedUser.username().trim(),
                seedUser.password(),
                role
        ));
    }
}
