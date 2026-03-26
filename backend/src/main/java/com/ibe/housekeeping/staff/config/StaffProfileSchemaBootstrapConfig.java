package com.ibe.housekeeping.staff.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@Order(0)
public class StaffProfileSchemaBootstrapConfig implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    public StaffProfileSchemaBootstrapConfig(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        jdbcTemplate.execute("""
                ALTER TABLE staff_profiles
                ADD COLUMN IF NOT EXISTS total_minutes_worked INTEGER NOT NULL DEFAULT 0
                """);
    }
}
