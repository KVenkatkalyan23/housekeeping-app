package com.ibe.housekeeping.admin.taskreassignment;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibe.housekeeping.activitylog.repository.ActivityLogRepository;
import com.ibe.housekeeping.allocation.repository.TaskAssignmentRepository;
import com.ibe.housekeeping.auth.dto.LoginRequest;
import com.ibe.housekeeping.auth.repository.UserRepository;
import com.ibe.housekeeping.common.enums.AssignmentSource;
import com.ibe.housekeeping.common.enums.AvailabilityStatus;
import com.ibe.housekeeping.common.enums.Role;
import com.ibe.housekeeping.common.enums.RoomStatus;
import com.ibe.housekeeping.common.enums.TaskStatus;
import com.ibe.housekeeping.common.enums.TaskType;
import com.ibe.housekeeping.entity.ActivityLog;
import com.ibe.housekeeping.entity.CleaningTask;
import com.ibe.housekeeping.entity.Room;
import com.ibe.housekeeping.entity.Shift;
import com.ibe.housekeeping.entity.StaffProfile;
import com.ibe.housekeeping.entity.TaskAssignment;
import com.ibe.housekeeping.entity.User;
import com.ibe.housekeeping.room.repository.RoomRepository;
import com.ibe.housekeeping.shift.repository.ShiftRepository;
import com.ibe.housekeeping.staff.repository.StaffProfileRepository;
import com.ibe.housekeeping.support.FixedClockTestConfig;
import com.ibe.housekeeping.task.repository.CleaningTaskRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Comparator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(FixedClockTestConfig.class)
class AdminTaskReassignmentControllerIntegrationTest {

    private static final LocalDate FIXED_TODAY = LocalDate.of(2026, 3, 27);

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StaffProfileRepository staffProfileRepository;

    @Autowired
    private ShiftRepository shiftRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private CleaningTaskRepository cleaningTaskRepository;

    @Autowired
    private TaskAssignmentRepository taskAssignmentRepository;

    @Autowired
    private ActivityLogRepository activityLogRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String adminToken;
    private String staffToken;
    private Shift morningShift;
    private Shift afternoonShift;
    private StaffProfile currentStaff;
    private StaffProfile targetStaff;
    private StaffProfile overloadedStaff;
    private StaffProfile afternoonStaff;
    private CleaningTask assignedTask;
    private CleaningTask completedTask;

    @BeforeEach
    void setUp() throws Exception {
        activityLogRepository.deleteAll();
        taskAssignmentRepository.deleteAll();
        cleaningTaskRepository.deleteAll();
        roomRepository.deleteAll();
        staffProfileRepository.deleteAll();
        shiftRepository.deleteAll();
        userRepository.deleteAll();

        User admin = userRepository.save(User.builder()
                .username("admin-user")
                .password(passwordEncoder.encode("password123"))
                .role(Role.ADMIN)
                .build());

        User staff = userRepository.save(User.builder()
                .username("staff-user")
                .password(passwordEncoder.encode("password123"))
                .role(Role.STAFF)
                .build());

        morningShift = shiftRepository.save(Shift.builder()
                .shiftCode("MORN")
                .shiftName("Morning")
                .startTime(LocalTime.of(8, 0))
                .endTime(LocalTime.of(12, 0))
                .durationMinutes(240)
                .build());

        afternoonShift = shiftRepository.save(Shift.builder()
                .shiftCode("AFT")
                .shiftName("Afternoon")
                .startTime(LocalTime.of(13, 0))
                .endTime(LocalTime.of(17, 0))
                .durationMinutes(240)
                .build());

        currentStaff = createStaff("elena-user", "Elena Rodriguez", morningShift);
        targetStaff = createStaff("noah-user", "Noah Bennett", morningShift);
        overloadedStaff = createStaff("iris-user", "Iris Carter", morningShift);
        afternoonStaff = createStaff("marcus-user", "Marcus Chen", afternoonShift);
        createStaff("staff-user-profile", "Staff User", morningShift);

        seedTasks();

        adminToken = authenticate(admin.getUsername(), "password123");
        staffToken = authenticate(staff.getUsername(), "password123");
    }

    @Test
    void reassignTaskSucceedsAndCreatesAuditEntry() throws Exception {
        mockMvc.perform(post("/api/admin/tasks/reassign")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "taskId": "%s",
                                  "targetStaffId": "%s"
                                }
                                """.formatted(assignedTask.getId(), targetStaff.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taskId").value(assignedTask.getId().toString()))
                .andExpect(jsonPath("$.roomNumber").value("402"))
                .andExpect(jsonPath("$.previousStaffId").value(currentStaff.getId().toString()))
                .andExpect(jsonPath("$.newStaffId").value(targetStaff.getId().toString()))
                .andExpect(jsonPath("$.assignmentSource").value("MANUAL"))
                .andExpect(jsonPath("$.taskStatus").value("ASSIGNED"))
                .andExpect(jsonPath("$.successMessage").value("Task reassigned successfully."));

        TaskAssignment updatedAssignment = taskAssignmentRepository.findByCleaningTaskIdForReassignment(assignedTask.getId())
                .orElseThrow();
        assertThat(updatedAssignment.getStaff().getId()).isEqualTo(targetStaff.getId());
        assertThat(updatedAssignment.getAssignmentSource()).isEqualTo(AssignmentSource.MANUAL);

        ActivityLog auditEntry = activityLogRepository.findAll().stream()
                .max(Comparator.comparing(ActivityLog::getCreatedAt))
                .orElseThrow();
        assertThat(auditEntry.getEventCode()).isEqualTo("TASK_REASSIGNED");
        assertThat(auditEntry.getActorUserId()).isNotNull();
        assertThat(auditEntry.getMetadata()).contains("MANUAL");
        assertThat(auditEntry.getMetadata()).contains(currentStaff.getFullName());
        assertThat(auditEntry.getMetadata()).contains(targetStaff.getFullName());
    }

    @Test
    void reassignmentCandidatesReturnOnlyCapacitySafeSameShiftStaff() throws Exception {
        mockMvc.perform(get("/api/admin/tasks/reassign/candidates")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("taskId", assignedTask.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(4))
                .andExpect(jsonPath("$[0].fullName").value("Elena Rodriguez"))
                .andExpect(jsonPath("$[1].fullName").value("Marcus Chen"))
                .andExpect(jsonPath("$[2].fullName").value("Noah Bennett"))
                .andExpect(jsonPath("$[3].fullName").value("Staff User"));
    }

    @Test
    void reassignTaskRejectsInvalidRequestsAndProtectsAccess() throws Exception {
        mockMvc.perform(post("/api/admin/tasks/reassign")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "taskId": "%s",
                                  "targetStaffId": "%s"
                                }
                                """.formatted(assignedTask.getId(), currentStaff.getId())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.message").value(
                        "Selected staff member must be different from the current assignee."
                ));

        mockMvc.perform(post("/api/admin/tasks/reassign")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "taskId": "%s",
                                  "targetStaffId": "%s"
                                }
                                """.formatted(assignedTask.getId(), overloadedStaff.getId())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.message").value(
                        "Selected staff member does not have sufficient remaining capacity for this task."
                ));


        mockMvc.perform(post("/api/admin/tasks/reassign")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "taskId": "%s",
                                  "targetStaffId": "%s"
                                }
                                """.formatted(completedTask.getId(), targetStaff.getId())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.message").value(
                        "Completed or cancelled tasks cannot be reassigned."
                ));

        mockMvc.perform(post("/api/admin/tasks/reassign")
                        .header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "taskId": "%s",
                                  "targetStaffId": "%s"
                                }
                                """.formatted(assignedTask.getId(), targetStaff.getId())))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.message").value("Access denied"));

        mockMvc.perform(post("/api/admin/tasks/reassign")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "taskId": "%s",
                                  "targetStaffId": "%s"
                                }
                                """.formatted(assignedTask.getId(), targetStaff.getId())))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.message").value("Authentication required"));
    }

    @Test
    void reassignTaskAllowsCrossShiftWhenDailyCapacityRemainsWithinLimit() throws Exception {
        mockMvc.perform(post("/api/admin/tasks/reassign")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "taskId": "%s",
                                  "targetStaffId": "%s"
                                }
                                """.formatted(assignedTask.getId(), afternoonStaff.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.newStaffId").value(afternoonStaff.getId().toString()))
                .andExpect(jsonPath("$.assignmentSource").value("MANUAL"));
    }
    private StaffProfile createStaff(String username, String fullName, Shift shift) {
        User user = userRepository.save(User.builder()
                .username(username)
                .password(passwordEncoder.encode("password123"))
                .role(Role.STAFF)
                .build());

        return staffProfileRepository.save(StaffProfile.builder()
                .user(user)
                .fullName(fullName)
                .email(username + "@housekeeping.local")
                .phone("9999999999")
                .preferredShift(shift)
                .availabilityStatus(AvailabilityStatus.ON_DUTY)
                .build());
    }

    private void seedTasks() {
        Room room402 = roomRepository.save(Room.builder().roomNumber(402).roomStatus(RoomStatus.ACTIVE).build());
        Room room502 = roomRepository.save(Room.builder().roomNumber(502).roomStatus(RoomStatus.ACTIVE).build());
        Room room602 = roomRepository.save(Room.builder().roomNumber(602).roomStatus(RoomStatus.ACTIVE).build());
        Room room702 = roomRepository.save(Room.builder().roomNumber(702).roomStatus(RoomStatus.ACTIVE).build());

        assignedTask = cleaningTaskRepository.save(CleaningTask.builder()
                .room(room402)
                .taskDate(FIXED_TODAY)
                .shift(morningShift)
                .taskType(TaskType.DEEP_CLEAN)
                .priorityOrder(1)
                .estimatedMinutes(120)
                .taskStatus(TaskStatus.ASSIGNED)
                .build());

        completedTask = cleaningTaskRepository.save(CleaningTask.builder()
                .room(room502)
                .taskDate(FIXED_TODAY)
                .shift(morningShift)
                .taskType(TaskType.DAILY_CLEAN)
                .priorityOrder(2)
                .estimatedMinutes(30)
                .taskStatus(TaskStatus.COMPLETED)
                .build());

        CleaningTask overloadedTask = cleaningTaskRepository.save(CleaningTask.builder()
                .room(room602)
                .taskDate(FIXED_TODAY)
                .shift(morningShift)
                .taskType(TaskType.DAILY_CLEAN)
                .priorityOrder(3)
                .estimatedMinutes(150)
                .taskStatus(TaskStatus.ASSIGNED)
                .build());

        CleaningTask afternoonAssignedTask = cleaningTaskRepository.save(CleaningTask.builder()
                .room(room702)
                .taskDate(FIXED_TODAY)
                .shift(afternoonShift)
                .taskType(TaskType.DAILY_CLEAN)
                .priorityOrder(4)
                .estimatedMinutes(45)
                .taskStatus(TaskStatus.ASSIGNED)
                .build());

        taskAssignmentRepository.save(TaskAssignment.builder()
                .cleaningTask(assignedTask)
                .staff(currentStaff)
                .build());

        taskAssignmentRepository.save(TaskAssignment.builder()
                .cleaningTask(completedTask)
                .staff(currentStaff)
                .build());

        taskAssignmentRepository.save(TaskAssignment.builder()
                .cleaningTask(overloadedTask)
                .staff(overloadedStaff)
                .build());

        taskAssignmentRepository.save(TaskAssignment.builder()
                .cleaningTask(afternoonAssignedTask)
                .staff(afternoonStaff)
                .build());
    }

    private String authenticate(String username, String password) throws Exception {
        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest(username, password))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode jsonNode = objectMapper.readTree(response);
        return jsonNode.get("accessToken").asText();
    }
}


