package com.ibe.housekeeping.task.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record GenerateTasksRequest(@NotNull LocalDate taskDate) {
}
