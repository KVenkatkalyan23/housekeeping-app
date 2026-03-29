package com.ibe.housekeeping.allocation.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@Order(0)
public class TaskAssignmentSchemaBootstrapConfig implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    public TaskAssignmentSchemaBootstrapConfig(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        jdbcTemplate.execute("""
                ALTER TABLE task_assignments
                ADD COLUMN IF NOT EXISTS assignment_source VARCHAR(20) NOT NULL DEFAULT 'AUTO'
                """);
    }
}
