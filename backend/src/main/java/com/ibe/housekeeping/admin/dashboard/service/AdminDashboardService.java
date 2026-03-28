package com.ibe.housekeeping.admin.dashboard.service;

import com.ibe.housekeeping.admin.dashboard.dto.AdminDashboardResponse;
import com.ibe.housekeeping.auth.repository.UserRepository;
import com.ibe.housekeeping.common.enums.AvailabilityStatus;
import com.ibe.housekeeping.common.enums.TaskStatus;
import com.ibe.housekeeping.entity.CleaningTask;
import com.ibe.housekeeping.entity.Shift;
import com.ibe.housekeeping.entity.StaffProfile;
import com.ibe.housekeeping.room.repository.RoomRepository;
import com.ibe.housekeeping.roomstay.repository.RoomStayRepository;
import com.ibe.housekeeping.staff.repository.StaffProfileRepository;
import com.ibe.housekeeping.task.repository.CleaningTaskRepository;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;

@Service
public class AdminDashboardService {

    private static final List<ChartSlot> CHART_SLOTS = List.of(
            new ChartSlot("6AM", LocalTime.of(6, 0), LocalTime.of(9, 0), false),
            new ChartSlot("9AM", LocalTime.of(9, 0), LocalTime.of(12, 0), false),
            new ChartSlot("12PM", LocalTime.of(12, 0), LocalTime.of(15, 0), false),
            new ChartSlot("3PM (PEAK)", LocalTime.of(15, 0), LocalTime.of(18, 0), true),
            new ChartSlot("6PM", LocalTime.of(18, 0), LocalTime.of(21, 0), false),
            new ChartSlot("9PM", LocalTime.of(21, 0), LocalTime.of(23, 59), false)
    );

    private final CleaningTaskRepository cleaningTaskRepository;
    private final StaffProfileRepository staffProfileRepository;
    private final RoomRepository roomRepository;
    private final RoomStayRepository roomStayRepository;
    private final UserRepository userRepository;
    private final Clock clock;

    public AdminDashboardService(
            CleaningTaskRepository cleaningTaskRepository,
            StaffProfileRepository staffProfileRepository,
            RoomRepository roomRepository,
            RoomStayRepository roomStayRepository,
            UserRepository userRepository,
            Clock clock
    ) {
        this.cleaningTaskRepository = cleaningTaskRepository;
        this.staffProfileRepository = staffProfileRepository;
        this.roomRepository = roomRepository;
        this.roomStayRepository = roomStayRepository;
        this.userRepository = userRepository;
        this.clock = clock;
    }

    public AdminDashboardResponse getDashboard(String username) {
        LocalDate today = LocalDate.now(clock);
        LocalDate yesterday = today.minusDays(1);

        List<CleaningTask> todaysTasks = cleaningTaskRepository.findDashboardTasksByTaskDate(today);
        List<CleaningTask> yesterdaysTasks = cleaningTaskRepository.findDashboardTasksByTaskDate(yesterday);
        List<StaffProfile> onDutyStaff = staffProfileRepository
                .findAllByAvailabilityStatusWithPreferredShift(AvailabilityStatus.ON_DUTY);

        long totalRooms = roomRepository.count();
        long occupiedRooms = roomStayRepository.countByCheckInDateLessThanEqualAndCheckOutDateGreaterThanEqual(today, today);
        long vacantRooms = Math.max(totalRooms - occupiedRooms, 0L);
        int occupancyRate = percentage(occupiedRooms, totalRooms);

        int requiredMinutes = activeTaskMinutes(todaysTasks);
        int availableMinutes = availableStaffMinutes(onDutyStaff);
        int utilizationPercent = percentage(requiredMinutes, availableMinutes);

        long completedTasks = countTasksWithStatus(todaysTasks, TaskStatus.COMPLETED);
        long totalAssignedTasks = countActiveTasks(todaysTasks);
        int completionRate = percentage(completedTasks, totalAssignedTasks);
        double yesterdayCompletionRate = percentage(countTasksWithStatus(yesterdaysTasks, TaskStatus.COMPLETED),
                countActiveTasks(yesterdaysTasks));
        double deltaVsYesterday = roundToSingleDecimal(completionRate - yesterdayCompletionRate);

        double requiredHours = roundToSingleDecimal(requiredMinutes / 60.0);
        double availableHours = roundToSingleDecimal(availableMinutes / 60.0);
        double deltaHours = roundToSingleDecimal(requiredHours - availableHours);
        int averageShiftMinutes = averageShiftMinutes(onDutyStaff);
        int additionalStaffRequired = deltaHours > 0
                ? (int) Math.ceil((deltaHours * 60.0) / Math.max(averageShiftMinutes, 1))
                : 0;
        int shortfallPercent = deltaHours > 0 ? percentage(deltaHours, requiredHours) : 0;

        return new AdminDashboardResponse(
                new AdminDashboardResponse.ShortfallAlert(
                        shortfallPercent,
                        buildShortfallMessage(deltaHours, additionalStaffRequired),
                        additionalStaffRequired,
                        deltaHours > 0
                ),
                new AdminDashboardResponse.InventoryStatus(
                        totalRooms,
                        occupiedRooms,
                        vacantRooms,
                        occupancyRate
                ),
                new AdminDashboardResponse.WorkforceEfficiency(
                        utilizationPercent,
                        "%d staff on duty are covering %d active cleaning tasks.".formatted(
                                onDutyStaff.size(),
                                totalAssignedTasks
                        )
                ),
                new AdminDashboardResponse.SlaPerformance(
                        completionRate,
                        deltaVsYesterday,
                        completedTasks,
                        totalAssignedTasks
                ),
                new AdminDashboardResponse.CapacityWorkload(
                        availableHours,
                        requiredHours,
                        buildCapacityWorkloadSeries(onDutyStaff, todaysTasks)
                ),
                new AdminDashboardResponse.ResourceDiscrepancy(
                        requiredHours,
                        availableHours,
                        deltaHours,
                        buildImpactMessage(deltaHours)
                ),
                new AdminDashboardResponse.CurrentAdmin(
                        resolveAdminDisplayName(username),
                        "MASTER ADMIN",
                        null
                )
        );
    }

    private List<AdminDashboardResponse.CapacityWorkloadPoint> buildCapacityWorkloadSeries(
            List<StaffProfile> onDutyStaff,
            List<CleaningTask> todaysTasks
    ) {
        return CHART_SLOTS.stream()
                .map(slot -> new AdminDashboardResponse.CapacityWorkloadPoint(
                        slot.label(),
                        roundToSingleDecimal(calculateAvailableHoursForSlot(onDutyStaff, slot)),
                        roundToSingleDecimal(calculateRequiredHoursForSlot(todaysTasks, slot)),
                        slot.peak()
                ))
                .toList();
    }

    private double calculateAvailableHoursForSlot(List<StaffProfile> onDutyStaff, ChartSlot slot) {
        return onDutyStaff.stream()
                .map(StaffProfile::getPreferredShift)
                .filter(shift -> shift != null)
                .mapToDouble(shift -> overlapHours(shift.getStartTime(), shift.getEndTime(), slot.startTime(), slot.endTime()))
                .sum();
    }

    private double calculateRequiredHoursForSlot(List<CleaningTask> todaysTasks, ChartSlot slot) {
        double totalHours = 0.0;

        for (CleaningTask task : todaysTasks) {
            if (task.getTaskStatus() == TaskStatus.CANCELLED) {
                continue;
            }

            Shift shift = task.getShift();
            if (shift == null) {
                if ("12PM".equals(slot.label())) {
                    totalHours += task.getEstimatedMinutes() / 60.0;
                }
                continue;
            }

            long overlapMinutes = overlapMinutes(
                    shift.getStartTime(),
                    shift.getEndTime(),
                    slot.startTime(),
                    slot.endTime()
            );

            if (overlapMinutes <= 0) {
                continue;
            }

            long shiftMinutes = Math.max(ChronoUnit.MINUTES.between(shift.getStartTime(), shift.getEndTime()), 1L);
            totalHours += (task.getEstimatedMinutes() * (overlapMinutes / (double) shiftMinutes)) / 60.0;
        }

        return totalHours;
    }

    private int activeTaskMinutes(List<CleaningTask> tasks) {
        return tasks.stream()
                .filter(task -> task.getTaskStatus() != TaskStatus.CANCELLED)
                .mapToInt(CleaningTask::getEstimatedMinutes)
                .sum();
    }

    private int availableStaffMinutes(List<StaffProfile> onDutyStaff) {
        return onDutyStaff.stream()
                .map(StaffProfile::getPreferredShift)
                .filter(shift -> shift != null)
                .mapToInt(Shift::getDurationMinutes)
                .sum();
    }

    private int averageShiftMinutes(List<StaffProfile> onDutyStaff) {
        return (int) Math.round(onDutyStaff.stream()
                .map(StaffProfile::getPreferredShift)
                .filter(shift -> shift != null)
                .mapToInt(Shift::getDurationMinutes)
                .average()
                .orElse(240.0));
    }

    private long countTasksWithStatus(List<CleaningTask> tasks, TaskStatus taskStatus) {
        return tasks.stream()
                .filter(task -> task.getTaskStatus() == taskStatus)
                .count();
    }

    private long countActiveTasks(List<CleaningTask> tasks) {
        return tasks.stream()
                .filter(task -> task.getTaskStatus() != TaskStatus.CANCELLED)
                .count();
    }

    private String buildShortfallMessage(double deltaHours, int additionalStaffRequired) {
        if (deltaHours <= 0) {
            return "Current shift capacity is healthy and sufficient for today’s workload.";
        }

        return "Current shift requires %d additional staff members to maintain service standards."
                .formatted(additionalStaffRequired);
    }

    private String buildImpactMessage(double deltaHours) {
        if (deltaHours <= 0) {
            return "Current roster availability is aligned with today’s cleaning demand.";
        }

        return "The delta of %.1f hours represents likely delayed room check-ins if not mitigated."
                .formatted(deltaHours);
    }

    private String resolveAdminDisplayName(String username) {
        return userRepository.findByUsername(username)
                .map(user -> formatDisplayName(user.getUsername()))
                .orElse(formatDisplayName(username));
    }

    private String formatDisplayName(String value) {
        String normalized = value.replace('-', ' ').replace('_', ' ').trim();
        if (normalized.isBlank()) {
            return "Admin User";
        }

        String[] segments = normalized.split("\\s+");
        StringBuilder builder = new StringBuilder();

        for (int index = 0; index < segments.length; index++) {
            if (index > 0) {
                builder.append(' ');
            }

            String segment = segments[index];
            builder.append(segment.substring(0, 1).toUpperCase(Locale.ENGLISH));
            builder.append(segment.substring(1).toLowerCase(Locale.ENGLISH));
        }

        return builder.toString();
    }

    private int percentage(long numerator, long denominator) {
        if (denominator <= 0) {
            return numerator > 0 ? 100 : 0;
        }

        return (int) Math.round((numerator * 100.0) / denominator);
    }

    private int percentage(double numerator, double denominator) {
        if (denominator <= 0) {
            return numerator > 0 ? 100 : 0;
        }

        return (int) Math.round((numerator * 100.0) / denominator);
    }

    private double roundToSingleDecimal(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    private double overlapHours(
            LocalTime sourceStart,
            LocalTime sourceEnd,
            LocalTime slotStart,
            LocalTime slotEnd
    ) {
        return overlapMinutes(sourceStart, sourceEnd, slotStart, slotEnd) / 60.0;
    }

    private long overlapMinutes(
            LocalTime sourceStart,
            LocalTime sourceEnd,
            LocalTime slotStart,
            LocalTime slotEnd
    ) {
        LocalTime effectiveStart = sourceStart.isAfter(slotStart) ? sourceStart : slotStart;
        LocalTime effectiveEnd = sourceEnd.isBefore(slotEnd) ? sourceEnd : slotEnd;

        if (!effectiveEnd.isAfter(effectiveStart)) {
            return 0L;
        }

        return ChronoUnit.MINUTES.between(effectiveStart, effectiveEnd);
    }

    private record ChartSlot(String label, LocalTime startTime, LocalTime endTime, boolean peak) {
    }
}
