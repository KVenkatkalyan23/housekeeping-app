package com.ibe.housekeeping.activitylog.repository;

import com.ibe.housekeeping.activitylog.entity.ActivityLog;
import com.ibe.housekeeping.common.enums.EventCategory;
import com.ibe.housekeeping.common.enums.Severity;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, UUID> {

    Page<ActivityLog> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<ActivityLog> findByEventCategoryOrderByCreatedAtDesc(EventCategory eventCategory, Pageable pageable);

    Page<ActivityLog> findByEventCategoryAndSeverityOrderByCreatedAtDesc(
            EventCategory eventCategory,
            Severity severity,
            Pageable pageable
    );

    Page<ActivityLog> findBySeverityOrderByCreatedAtDesc(Severity severity, Pageable pageable);

    Page<ActivityLog> findByEventCodeOrderByCreatedAtDesc(String eventCode, Pageable pageable);

    Page<ActivityLog> findByEventCategoryAndEventCodeOrderByCreatedAtDesc(
            EventCategory eventCategory,
            String eventCode,
            Pageable pageable
    );

    Page<ActivityLog> findBySeverityAndEventCodeOrderByCreatedAtDesc(
            Severity severity,
            String eventCode,
            Pageable pageable
    );

    Page<ActivityLog> findByEventCategoryAndSeverityAndEventCodeOrderByCreatedAtDesc(
            EventCategory eventCategory,
            Severity severity,
            String eventCode,
            Pageable pageable
    );

    boolean existsByEventCodeAndCreatedAtBetween(String eventCode, OffsetDateTime createdAtStart, OffsetDateTime createdAtEnd);
}
