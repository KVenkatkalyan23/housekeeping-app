package com.ibe.housekeeping.activitylog.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibe.housekeeping.activitylog.dto.CreateActivityLogRequest;
import com.ibe.housekeeping.activitylog.entity.ActivityLog;
import com.ibe.housekeeping.activitylog.repository.ActivityLogRepository;
import com.ibe.housekeeping.common.enums.ActorType;
import com.ibe.housekeeping.common.enums.EventCategory;
import com.ibe.housekeeping.common.enums.Severity;
import com.ibe.housekeeping.entity.Attendance;
import com.ibe.housekeeping.entity.CleaningTask;
import com.ibe.housekeeping.entity.Shift;
import com.ibe.housekeeping.entity.StaffProfile;
import com.ibe.housekeeping.entity.TaskAssignment;
import com.ibe.housekeeping.entity.User;
import java.time.Clock;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ActivityLogService {

    private final ActivityLogRepository activityLogRepository;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    public ActivityLogService(
            ActivityLogRepository activityLogRepository,
            ObjectMapper objectMapper,
            Clock clock
    ) {
        this.activityLogRepository = activityLogRepository;
        this.objectMapper = objectMapper;
        this.clock = clock;
    }

    @Transactional
    public ActivityLog logEvent(CreateActivityLogRequest request) {
        ActivityLog activityLog = ActivityLog.builder()
                .eventCode(request.eventCode())
                .eventTitle(request.eventTitle())
                .eventMessage(request.eventMessage())
                .eventCategory(request.eventCategory())
                .severity(request.severity())
                .actorType(request.actorType())
                .actorUserId(request.actorUserId())
                .actorName(request.actorName())
                .targetEntityType(request.targetEntityType())
                .targetEntityId(request.targetEntityId())
                .relatedStaffId(request.relatedStaffId())
                .relatedRoomId(request.relatedRoomId())
                .metadata(serializeMetadata(request.metadata()))
                .build();

        return activityLogRepository.save(activityLog);
    }

    @Transactional
    public void logTaskAssigned(TaskAssignment assignment) {
        CleaningTask task = assignment.getCleaningTask();
        StaffProfile staff = assignment.getStaff();
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("actorSubtitle", "Automated Service");
        metadata.put("roomNumber", task.getRoom().getRoomNumber());
        metadata.put("staffName", staff.getFullName());
        metadata.put("taskType", task.getTaskType());
        metadata.put("shiftId", task.getShift() != null ? task.getShift().getId() : null);
        metadata.put("shiftLabel", task.getShift() != null ? task.getShift().getShiftName() : null);
        metadata.put("estimatedMinutes", task.getEstimatedMinutes());
        metadata.put("targetLabel", "Room " + task.getRoom().getRoomNumber());
        metadata.put("targetSubLabel", "Task ID: " + abbreviate(task.getId()));

        logEvent(CreateActivityLogRequest.builder()
                .eventCode("TASK_ASSIGNED")
                .eventTitle("Task Assigned")
                .eventMessage("Task for room %s assigned to %s.".formatted(task.getRoom().getRoomNumber(), staff.getFullName()))
                .eventCategory(EventCategory.TASK)
                .severity(Severity.SUCCESS)
                .actorType(ActorType.SYSTEM)
                .actorName("System")
                .targetEntityType("TASK")
                .targetEntityId(task.getId())
                .relatedStaffId(staff.getId())
                .relatedRoomId(task.getRoom().getId())
                .metadata(metadata)
                .build());
    }

    @Transactional
    public void logTaskCompleted(CleaningTask task, StaffProfile staffProfile, User actorUser) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("actorSubtitle", "Housekeeping Staff");
        metadata.put("roomNumber", task.getRoom().getRoomNumber());
        metadata.put("taskType", task.getTaskType());
        metadata.put("shiftId", task.getShift() != null ? task.getShift().getId() : null);
        metadata.put("shiftLabel", task.getShift() != null ? task.getShift().getShiftName() : null);
        metadata.put("completedAt", task.getCompletedAt());
        metadata.put("targetLabel", "Room " + task.getRoom().getRoomNumber());
        metadata.put("targetSubLabel", "Task ID: " + abbreviate(task.getId()));

        logEvent(CreateActivityLogRequest.builder()
                .eventCode("TASK_COMPLETED")
                .eventTitle("Task Completed")
                .eventMessage("%s completed the task for room %s.".formatted(
                        staffProfile.getFullName(),
                        task.getRoom().getRoomNumber()
                ))
                .eventCategory(EventCategory.TASK)
                .severity(Severity.SUCCESS)
                .actorType(ActorType.USER)
                .actorUserId(actorUser.getId())
                .actorName(staffProfile.getFullName())
                .targetEntityType("TASK")
                .targetEntityId(task.getId())
                .relatedStaffId(staffProfile.getId())
                .relatedRoomId(task.getRoom().getId())
                .metadata(metadata)
                .build());
    }

    @Transactional
    public void logTaskRelocated(CleaningTask task, StaffProfile previousStaff, StaffProfile newStaff, String triggerReason) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("actorSubtitle", "Automated Service");
        metadata.put("roomNumber", task.getRoom().getRoomNumber());
        metadata.put("oldStaffId", previousStaff.getId());
        metadata.put("oldStaffName", previousStaff.getFullName());
        metadata.put("newStaffId", newStaff.getId());
        metadata.put("newStaffName", newStaff.getFullName());
        metadata.put("triggerReason", triggerReason);
        metadata.put("shiftId", task.getShift() != null ? task.getShift().getId() : null);
        metadata.put("shiftLabel", task.getShift() != null ? task.getShift().getShiftName() : null);
        metadata.put("targetLabel", "Room " + task.getRoom().getRoomNumber());
        metadata.put("targetSubLabel", "Task ID: " + abbreviate(task.getId()));

        logEvent(CreateActivityLogRequest.builder()
                .eventCode("TASK_RELOCATED")
                .eventTitle("Room Assignment Change")
                .eventMessage("Task for room %s moved from %s to %s.".formatted(
                        task.getRoom().getRoomNumber(),
                        previousStaff.getFullName(),
                        newStaff.getFullName()
                ))
                .eventCategory(EventCategory.TASK)
                .severity(Severity.WARNING)
                .actorType(ActorType.SYSTEM)
                .actorName("System")
                .targetEntityType("TASK")
                .targetEntityId(task.getId())
                .relatedStaffId(newStaff.getId())
                .relatedRoomId(task.getRoom().getId())
                .metadata(metadata)
                .build());
    }

    @Transactional
    public void logClockIn(Attendance attendance, StaffProfile staffProfile, User actorUser) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("actorSubtitle", "Housekeeping Staff");
        metadata.put("clockInTime", attendance.getClockInTime());
        metadata.put("workDate", attendance.getWorkDate());
        metadata.put("shiftId", attendance.getShift().getId());
        metadata.put("shiftLabel", attendance.getShift().getShiftName());
        metadata.put("targetLabel", attendance.getShift().getShiftName());
        metadata.put("targetSubLabel", "Attendance ID: " + abbreviate(attendance.getId()));

        logEvent(CreateActivityLogRequest.builder()
                .eventCode("CLOCK_IN")
                .eventTitle("Clock In")
                .eventMessage("%s clocked in for %s.".formatted(
                        staffProfile.getFullName(),
                        attendance.getShift().getShiftName()
                ))
                .eventCategory(EventCategory.ATTENDANCE)
                .severity(Severity.SUCCESS)
                .actorType(ActorType.USER)
                .actorUserId(actorUser.getId())
                .actorName(staffProfile.getFullName())
                .targetEntityType("ATTENDANCE")
                .targetEntityId(attendance.getId())
                .relatedStaffId(staffProfile.getId())
                .metadata(metadata)
                .build());
    }

    @Transactional
    public void logClockOut(Attendance attendance, StaffProfile staffProfile, User actorUser) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("actorSubtitle", "Housekeeping Staff");
        metadata.put("clockOutTime", attendance.getClockOutTime());
        metadata.put("workedMinutes", attendance.getWorkedMinutes());
        metadata.put("workDate", attendance.getWorkDate());
        metadata.put("shiftId", attendance.getShift().getId());
        metadata.put("shiftLabel", attendance.getShift().getShiftName());
        metadata.put("targetLabel", attendance.getShift().getShiftName());
        metadata.put("targetSubLabel", "Attendance ID: " + abbreviate(attendance.getId()));

        logEvent(CreateActivityLogRequest.builder()
                .eventCode("CLOCK_OUT")
                .eventTitle("Clock Out")
                .eventMessage("%s clocked out after %s minutes.".formatted(
                        staffProfile.getFullName(),
                        attendance.getWorkedMinutes()
                ))
                .eventCategory(EventCategory.ATTENDANCE)
                .severity(Severity.INFO)
                .actorType(ActorType.USER)
                .actorUserId(actorUser.getId())
                .actorName(staffProfile.getFullName())
                .targetEntityType("ATTENDANCE")
                .targetEntityId(attendance.getId())
                .relatedStaffId(staffProfile.getId())
                .metadata(metadata)
                .build());
    }

    @Transactional
    public void logShortfallDetected(
            Shift shift,
            double requiredHours,
            double availableHours,
            double deltaHours,
            int additionalStaffRequired,
            LocalDate taskDate
    ) {
        String shiftLabel = shift != null ? shift.getShiftName() : "Current Operations";
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("actorSubtitle", "Automated Service");
        metadata.put("requiredHours", requiredHours);
        metadata.put("availableHours", availableHours);
        metadata.put("deltaHours", deltaHours);
        metadata.put("additionalStaffRequired", additionalStaffRequired);
        metadata.put("shiftLabel", shiftLabel);
        metadata.put("date", taskDate);
        metadata.put("targetLabel", shiftLabel);
        metadata.put("targetSubLabel", "Date: " + taskDate);

        logEvent(CreateActivityLogRequest.builder()
                .eventCode("SHORTFALL_DETECTED")
                .eventTitle("Missing Staff Warning")
                .eventMessage("Capacity shortfall detected for %s on %s.".formatted(shiftLabel, taskDate))
                .eventCategory(EventCategory.SYSTEM)
                .severity(Severity.WARNING)
                .actorType(ActorType.SYSTEM)
                .actorName("System")
                .targetEntityType("SHIFT")
                .targetEntityId(shift != null ? shift.getId() : null)
                .metadata(metadata)
                .build());
    }

    @Transactional
    public void logAllocationRun(LocalDate taskDate, int assignedCount, int unassignedCount, int totalTasks) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("actorSubtitle", "Automated Service");
        metadata.put("taskDate", taskDate);
        metadata.put("assignedCount", assignedCount);
        metadata.put("unassignedCount", unassignedCount);
        metadata.put("totalTasks", totalTasks);
        metadata.put("targetLabel", "Allocation Run");
        metadata.put("targetSubLabel", "Date: " + taskDate);

        logEvent(CreateActivityLogRequest.builder()
                .eventCode("ALLOCATION_RUN")
                .eventTitle("Automated Resource Check")
                .eventMessage("Allocation run completed for %s with %s assigned and %s unassigned tasks.".formatted(
                        taskDate,
                        assignedCount,
                        unassignedCount
                ))
                .eventCategory(EventCategory.SYSTEM)
                .severity(Severity.SUCCESS)
                .actorType(ActorType.SYSTEM)
                .actorName("System")
                .targetEntityType("ALLOCATION")
                .metadata(metadata)
                .build());
    }

    @Transactional(readOnly = true)
    public boolean hasEventCodeForToday(String eventCode) {
        ZoneId zoneId = clock.getZone();
        LocalDate today = LocalDate.now(clock);
        OffsetDateTime startOfDay = today.atStartOfDay(zoneId).toOffsetDateTime();
        OffsetDateTime endOfDay = today.plusDays(1).atStartOfDay(zoneId).toOffsetDateTime().minusNanos(1);
        return activityLogRepository.existsByEventCodeAndCreatedAtBetween(eventCode, startOfDay, endOfDay);
    }

    private String serializeMetadata(Map<String, Object> metadata) {
        if (metadata == null || metadata.isEmpty()) {
            return null;
        }

        try {
            return objectMapper.writeValueAsString(metadata);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Unable to serialize activity log metadata.", exception);
        }
    }

    private String abbreviate(UUID id) {
        return id == null ? "N/A" : id.toString().substring(0, 8);
    }
}
