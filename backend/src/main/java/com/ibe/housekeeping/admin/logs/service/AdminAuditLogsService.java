package com.ibe.housekeeping.admin.logs.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibe.housekeeping.activitylog.entity.ActivityLog;
import com.ibe.housekeeping.activitylog.repository.ActivityLogRepository;
import com.ibe.housekeeping.admin.logs.dto.AdminAuditLogItemResponse;
import com.ibe.housekeeping.admin.logs.dto.AdminAuditLogsResponse;
import com.ibe.housekeeping.common.enums.ActorType;
import com.ibe.housekeeping.common.enums.EventCategory;
import com.ibe.housekeeping.common.enums.Severity;
import java.io.IOException;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AdminAuditLogsService {

    private final ActivityLogRepository activityLogRepository;
    private final ObjectMapper objectMapper;

    public AdminAuditLogsService(
            ActivityLogRepository activityLogRepository,
            ObjectMapper objectMapper
    ) {
        this.activityLogRepository = activityLogRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public AdminAuditLogsResponse getLogs(int page, int size, String category, String eventCode, String severity) {
        validatePagination(page, size);

        EventCategory requestedCategory = parseCategory(category);
        Severity requestedSeverity = parseSeverity(severity);
        String requestedEventCode = normalizeEventCode(eventCode);
        Pageable pageable = PageRequest.of(page, size);

        Page<ActivityLog> logPage = findLogs(requestedCategory, requestedSeverity, requestedEventCode, pageable);

        return new AdminAuditLogsResponse(
                logPage.getContent().stream().map(this::toItemResponse).toList(),
                logPage.getNumber(),
                logPage.getSize(),
                logPage.getTotalElements(),
                logPage.getTotalPages()
        );
    }

    private Page<ActivityLog> findLogs(
            EventCategory category,
            Severity severity,
            String eventCode,
            Pageable pageable
    ) {
        if (category != null && severity != null && eventCode != null) {
            return activityLogRepository.findByEventCategoryAndSeverityAndEventCodeOrderByCreatedAtDesc(
                    category,
                    severity,
                    eventCode,
                    pageable
            );
        }

        if (category != null && severity != null) {
            return activityLogRepository.findByEventCategoryAndSeverityOrderByCreatedAtDesc(category, severity, pageable);
        }

        if (category != null && eventCode != null) {
            return activityLogRepository.findByEventCategoryAndEventCodeOrderByCreatedAtDesc(category, eventCode, pageable);
        }

        if (severity != null && eventCode != null) {
            return activityLogRepository.findBySeverityAndEventCodeOrderByCreatedAtDesc(severity, eventCode, pageable);
        }

        if (category != null) {
            return activityLogRepository.findByEventCategoryOrderByCreatedAtDesc(category, pageable);
        }

        if (severity != null) {
            return activityLogRepository.findBySeverityOrderByCreatedAtDesc(severity, pageable);
        }

        if (eventCode != null) {
            return activityLogRepository.findByEventCodeOrderByCreatedAtDesc(eventCode, pageable);
        }

        return activityLogRepository.findAllByOrderByCreatedAtDesc(pageable);
    }

    private AdminAuditLogItemResponse toItemResponse(ActivityLog log) {
        Map<String, Object> metadata = readMetadata(log.getMetadata());
        String actorSubtitle = readString(metadata, "actorSubtitle");
        String targetLabel = readString(metadata, "targetLabel");
        String targetSubLabel = readString(metadata, "targetSubLabel");

        if (!StringUtils.hasText(actorSubtitle) && log.getActorType() == ActorType.SYSTEM) {
            actorSubtitle = "Automated Service";
        }

        if (!StringUtils.hasText(targetLabel)) {
            targetLabel = StringUtils.hasText(log.getTargetEntityType())
                    ? log.getTargetEntityType().toUpperCase(Locale.ENGLISH)
                    : "N/A";
        }

        if (!StringUtils.hasText(targetSubLabel) && log.getTargetEntityId() != null) {
            targetSubLabel = "ID: " + abbreviate(log.getTargetEntityId().toString());
        }

        return new AdminAuditLogItemResponse(
                log.getId(),
                log.getCreatedAt(),
                log.getEventCode(),
                log.getEventTitle(),
                log.getEventMessage(),
                log.getEventCategory(),
                log.getSeverity(),
                StringUtils.hasText(log.getActorName()) ? log.getActorName() : "Unknown",
                actorSubtitle,
                targetLabel,
                targetSubLabel
        );
    }

    private Map<String, Object> readMetadata(String metadata) {
        if (!StringUtils.hasText(metadata)) {
            return Collections.emptyMap();
        }

        try {
            return objectMapper.readValue(metadata, new TypeReference<>() {
            });
        } catch (IOException exception) {
            return Collections.emptyMap();
        }
    }

    private String readString(Map<String, Object> metadata, String key) {
        Object value = metadata.get(key);
        return value == null ? null : value.toString();
    }

    private EventCategory parseCategory(String category) {
        if (!StringUtils.hasText(category) || "ALL".equalsIgnoreCase(category.trim())) {
            return null;
        }

        try {
            return EventCategory.valueOf(category.trim().toUpperCase(Locale.ENGLISH));
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "category must be one of ALL, TASK, ATTENDANCE, LEAVE, or SYSTEM."
            );
        }
    }

    private Severity parseSeverity(String severity) {
        if (!StringUtils.hasText(severity)) {
            return null;
        }

        try {
            return Severity.valueOf(severity.trim().toUpperCase(Locale.ENGLISH));
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "severity must be one of SUCCESS, INFO, WARNING, or ERROR."
            );
        }
    }

    private String normalizeEventCode(String eventCode) {
        return StringUtils.hasText(eventCode) ? eventCode.trim().toUpperCase(Locale.ENGLISH) : null;
    }

    private void validatePagination(int page, int size) {
        if (page < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "page must be 0 or greater.");
        }

        if (size < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "size must be at least 1.");
        }
    }

    private String abbreviate(String value) {
        return value.length() <= 8 ? value : value.substring(0, 8);
    }
}
