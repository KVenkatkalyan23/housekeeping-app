package com.ibe.housekeeping.allocation.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record RunAllocationRequest(
        @NotNull LocalDate taskDate
) {
}
