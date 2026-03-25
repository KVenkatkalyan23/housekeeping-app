package com.ibe.housekeeping.common.api;

import java.time.Instant;
import java.util.List;

public record ErrorResponse(
        String message,
        int status,
        Instant timestamp,
        List<String> details
) {
}
