package com.ibe.housekeeping.staffworkboard.dto;

import java.time.LocalDate;
import java.util.List;

public record MyAssignedTasksResponse(
        LocalDate date,
        List<MyAssignedTaskItemResponse> tasks
) {
}
