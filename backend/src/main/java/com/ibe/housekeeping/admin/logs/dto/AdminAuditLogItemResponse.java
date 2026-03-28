package com.ibe.housekeeping.admin.logs.dto;

import com.ibe.housekeeping.common.enums.EventCategory;
import com.ibe.housekeeping.common.enums.Severity;
import java.time.OffsetDateTime;
import java.util.UUID;

public record AdminAuditLogItemResponse(
        UUID id,
        OffsetDateTime createdAt,
        String eventCode,
        String eventTitle,
        String eventMessage,
        EventCategory eventCategory,
        Severity severity,
        String actorName,
        String actorSubtitle,
        String targetLabel,
        String targetSubLabel
) {
}
