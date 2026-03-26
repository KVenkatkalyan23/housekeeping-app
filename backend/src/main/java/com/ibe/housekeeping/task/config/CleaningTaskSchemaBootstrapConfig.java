package com.ibe.housekeeping.task.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@Order(0)
public class CleaningTaskSchemaBootstrapConfig implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    public CleaningTaskSchemaBootstrapConfig(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        jdbcTemplate.execute("ALTER TABLE cleaning_tasks ALTER COLUMN shift_id DROP NOT NULL");
    }
}
