package com.ibe.housekeeping.activitylog.dto;

import com.ibe.housekeeping.common.enums.ActorType;
import com.ibe.housekeeping.common.enums.EventCategory;
import com.ibe.housekeeping.common.enums.Severity;
import java.util.Map;
import java.util.UUID;
import lombok.Builder;

@Builder
public record CreateActivityLogRequest(
        String eventCode,
        String eventTitle,
        String eventMessage,
        EventCategory eventCategory,
        Severity severity,
        ActorType actorType,
        UUID actorUserId,
        String actorName,
        String targetEntityType,
        UUID targetEntityId,
        UUID relatedStaffId,
        UUID relatedRoomId,
        Map<String, Object> metadata
) {
}
