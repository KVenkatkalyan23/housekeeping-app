package com.ibe.housekeeping.allocation.service;

import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;
import org.springframework.stereotype.Component;

@Component
public class TaskDateLockService {

    private final Map<LocalDate, ReentrantLock> locks = new ConcurrentHashMap<>();

    public <T> T executeWithTaskDateLock(LocalDate taskDate, Supplier<T> work) {
        ReentrantLock lock = locks.computeIfAbsent(taskDate, ignored -> new ReentrantLock());
        lock.lock();
        try {
            return work.get();
        } finally {
            lock.unlock();
            if (!lock.isLocked() && !lock.hasQueuedThreads()) {
                locks.remove(taskDate, lock);
            }
        }
    }
}
