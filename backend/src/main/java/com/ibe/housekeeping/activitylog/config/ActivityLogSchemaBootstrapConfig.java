package com.ibe.housekeeping.activitylog.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@Order(0)
public class ActivityLogSchemaBootstrapConfig implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    public ActivityLogSchemaBootstrapConfig(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        try {
            jdbcTemplate.execute("""
                    CREATE TABLE IF NOT EXISTS activity_logs (
                        id UUID PRIMARY KEY,
                        event_code VARCHAR(50) NOT NULL,
                        event_title VARCHAR(150) NOT NULL,
                        event_message TEXT NOT NULL,
                        event_category VARCHAR(30) NOT NULL,
                        severity VARCHAR(20) NOT NULL,
                        actor_type VARCHAR(20) NOT NULL,
                        actor_user_id UUID NULL,
                        actor_name VARCHAR(150) NULL,
                        target_entity_type VARCHAR(50) NULL,
                        target_entity_id UUID NULL,
                        related_staff_id UUID NULL,
                        related_room_id UUID NULL,
                        metadata JSONB NULL,
                        created_at TIMESTAMPTZ NOT NULL DEFAULT now()
                    )
                    """);
        } catch (DataAccessException exception) {
            jdbcTemplate.execute("""
                    CREATE TABLE IF NOT EXISTS activity_logs (
                        id UUID PRIMARY KEY,
                        event_code VARCHAR(50) NOT NULL,
                        event_title VARCHAR(150) NOT NULL,
                        event_message TEXT NOT NULL,
                        event_category VARCHAR(30) NOT NULL,
                        severity VARCHAR(20) NOT NULL,
                        actor_type VARCHAR(20) NOT NULL,
                        actor_user_id UUID NULL,
                        actor_name VARCHAR(150) NULL,
                        target_entity_type VARCHAR(50) NULL,
                        target_entity_id UUID NULL,
                        related_staff_id UUID NULL,
                        related_room_id UUID NULL,
                        metadata JSON NULL,
                        created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
                    )
                    """);
        }
        jdbcTemplate.execute("""
                CREATE INDEX IF NOT EXISTS idx_activity_logs_created_at
                ON activity_logs (created_at DESC)
                """);
        jdbcTemplate.execute("""
                CREATE INDEX IF NOT EXISTS idx_activity_logs_entity
                ON activity_logs (target_entity_type, target_entity_id)
                """);
        jdbcTemplate.execute("""
                CREATE INDEX IF NOT EXISTS idx_activity_logs_category_created_at
                ON activity_logs (event_category, created_at DESC)
                """);
    }
}
