package com.ibe.housekeeping.admin.taskreassignment.dto;

import java.util.UUID;

public record ReassignmentCandidateItemResponse(
        UUID staffId,
        String fullName,
        String shiftName,
        int allocatedMinutes,
        int remainingMinutes,
        boolean capacityAvailable
) {
}
