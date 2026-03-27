package com.ibe.housekeeping.allocation.service;

import java.time.Clock;
import java.time.LocalDate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class DailyAllocationScheduler {

    private final TaskAllocationService taskAllocationService;
    private final Clock clock;

    public DailyAllocationScheduler(TaskAllocationService taskAllocationService, Clock clock) {
        this.taskAllocationService = taskAllocationService;
        this.clock = clock;
    }

    @Scheduled(cron = "0 0 6 * * *", zone = "Asia/Kolkata")
    public void runDailyAllocation() {
        taskAllocationService.runScheduledAllocation(LocalDate.now(clock));
    }
}
