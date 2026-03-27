package com.ibe.housekeeping.allocation;

import com.ibe.housekeeping.allocation.service.DailyAllocationScheduler;
import com.ibe.housekeeping.allocation.service.TaskAllocationService;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class DailyAllocationSchedulerTest {

    @Test
    void runDailyAllocationInvokesScheduledAllocationForToday() {
        TaskAllocationService taskAllocationService = mock(TaskAllocationService.class);
        Clock clock = Clock.fixed(Instant.parse("2026-03-27T05:30:00Z"), ZoneId.of("Asia/Kolkata"));
        DailyAllocationScheduler scheduler = new DailyAllocationScheduler(taskAllocationService, clock);

        LocalDate today = LocalDate.now(clock);
        scheduler.runDailyAllocation();

        verify(taskAllocationService).runScheduledAllocation(eq(today));
    }
}
