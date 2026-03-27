package com.ibe.housekeeping.staffworkboard;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibe.housekeeping.allocation.repository.TaskAssignmentRepository;
import com.ibe.housekeeping.auth.dto.LoginRequest;
import com.ibe.housekeeping.auth.repository.UserRepository;
import com.ibe.housekeeping.common.enums.AvailabilityStatus;
import com.ibe.housekeeping.common.enums.Role;
import com.ibe.housekeeping.common.enums.RoomStatus;
import com.ibe.housekeeping.common.enums.TaskStatus;
import com.ibe.housekeeping.common.enums.TaskType;
import com.ibe.housekeeping.entity.CleaningTask;
import com.ibe.housekeeping.entity.Room;
import com.ibe.housekeeping.entity.Shift;
import com.ibe.housekeeping.entity.StaffProfile;
import com.ibe.housekeeping.entity.TaskAssignment;
import com.ibe.housekeeping.entity.User;
import com.ibe.housekeeping.room.repository.RoomRepository;
import com.ibe.housekeeping.shift.repository.ShiftRepository;
import com.ibe.housekeeping.staff.repository.StaffProfileRepository;
import com.ibe.housekeeping.task.repository.CleaningTaskRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
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
class StaffWorkboardControllerIntegrationTest {

    private static final LocalDate TODAY = LocalDate.now();

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StaffProfileRepository staffProfileRepository;

    @Autowired
    private TaskAssignmentRepository taskAssignmentRepository;

    @Autowired
    private CleaningTaskRepository cleaningTaskRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private ShiftRepository shiftRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String staffToken;
    private String otherStaffToken;
    private String adminToken;
    private StaffProfile staffProfile;
    private StaffProfile otherStaffProfile;
    private Shift morningShift;

    @BeforeEach
    void setUp() throws Exception {
        taskAssignmentRepository.deleteAll();
        cleaningTaskRepository.deleteAll();
        roomRepository.deleteAll();
        staffProfileRepository.deleteAll();
        shiftRepository.deleteAll();
        userRepository.deleteAll();

        User admin = userRepository.save(User.builder()
                .username("admin")
                .password(passwordEncoder.encode("password123"))
                .role(Role.ADMIN)
                .build());
        User staffUser = userRepository.save(User.builder()
                .username("staff-1")
                .password(passwordEncoder.encode("password123"))
                .role(Role.STAFF)
                .build());
        User otherStaffUser = userRepository.save(User.builder()
                .username("staff-2")
                .password(passwordEncoder.encode("password123"))
                .role(Role.STAFF)
                .build());

        morningShift = shiftRepository.save(Shift.builder()
                .shiftCode("MORN")
                .shiftName("Morning Shift")
                .startTime(LocalTime.of(8, 0))
                .endTime(LocalTime.of(12, 0))
                .durationMinutes(240)
                .build());

        staffProfile = staffProfileRepository.save(StaffProfile.builder()
                .user(staffUser)
                .fullName("Staff One")
                .email("staff1@test.local")
                .phone("1111111111")
                .preferredShift(morningShift)
                .availabilityStatus(AvailabilityStatus.OFF_DUTY)
                .build());

        otherStaffProfile = staffProfileRepository.save(StaffProfile.builder()
                .user(otherStaffUser)
                .fullName("Staff Two")
                .email("staff2@test.local")
                .phone("2222222222")
                .preferredShift(morningShift)
                .availabilityStatus(AvailabilityStatus.OFF_DUTY)
                .build());

        staffToken = authenticate("staff-1", "password123");
        otherStaffToken = authenticate("staff-2", "password123");
        adminToken = authenticate(admin.getUsername(), "password123");
    }

    @Test
    void todayTasksAndWorkloadReturnOnlyLoggedInStaffAssignments() throws Exception {
        CleaningTask assignedPending = assignTask(staffProfile, 301, TaskType.DAILY_CLEAN, 30, TaskStatus.ASSIGNED, TODAY);
        CleaningTask assignedCompleted = assignTask(staffProfile, 302, TaskType.DEEP_CLEAN, 120, TaskStatus.COMPLETED, TODAY);
        assignTask(otherStaffProfile, 303, TaskType.VACANT_CLEAN, 15, TaskStatus.ASSIGNED, TODAY);
        assignTask(staffProfile, 304, TaskType.DAILY_CLEAN, 30, TaskStatus.ASSIGNED, TODAY.plusDays(1));

        mockMvc.perform(get("/api/staff/tasks/today")
                        .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.date").value(TODAY.toString()))
                .andExpect(jsonPath("$.tasks.length()").value(2))
                .andExpect(jsonPath("$.tasks[0].taskId").value(assignedPending.getId().toString()))
                .andExpect(jsonPath("$.tasks[0].roomNumber").value(301))
                .andExpect(jsonPath("$.tasks[1].taskId").value(assignedCompleted.getId().toString()))
                .andExpect(jsonPath("$.tasks[1].completedAt").isNotEmpty());

        mockMvc.perform(get("/api/staff/tasks/workload")
                        .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assignedMinutes").value(150))
                .andExpect(jsonPath("$.completedMinutes").value(120))
                .andExpect(jsonPath("$.pendingMinutes").value(30))
                .andExpect(jsonPath("$.totalTaskCount").value(2))
                .andExpect(jsonPath("$.completedTaskCount").value(1))
                .andExpect(jsonPath("$.pendingTaskCount").value(1))
                .andExpect(jsonPath("$.completionPercentage").value(50));
    }

    @Test
    void markTaskCompleteUpdatesStatusAndTimestamp() throws Exception {
        staffProfile.setAvailabilityStatus(AvailabilityStatus.ON_DUTY);
        staffProfileRepository.save(staffProfile);
        CleaningTask task = assignTask(staffProfile, 401, TaskType.DAILY_CLEAN, 30, TaskStatus.ASSIGNED, TODAY);

        mockMvc.perform(post("/api/staff/tasks/" + task.getId() + "/complete")
                        .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taskId").value(task.getId().toString()))
                .andExpect(jsonPath("$.taskStatus").value("COMPLETED"))
                .andExpect(jsonPath("$.completedAt").isNotEmpty());

        CleaningTask savedTask = cleaningTaskRepository.findById(task.getId()).orElseThrow();
        assertThat(savedTask.getTaskStatus()).isEqualTo(TaskStatus.COMPLETED);
        assertThat(savedTask.getCompletedAt()).isNotNull();

        mockMvc.perform(get("/api/staff/tasks/workload")
                        .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assignedMinutes").value(30))
                .andExpect(jsonPath("$.completedMinutes").value(30))
                .andExpect(jsonPath("$.pendingMinutes").value(0))
                .andExpect(jsonPath("$.completedTaskCount").value(1))
                .andExpect(jsonPath("$.pendingTaskCount").value(0));
    }

    @Test
    void markTaskCompleteRejectsTasksAssignedToAnotherStaffOrAlreadyCompleted() throws Exception {
        staffProfile.setAvailabilityStatus(AvailabilityStatus.ON_DUTY);
        staffProfileRepository.save(staffProfile);
        CleaningTask otherStaffTask = assignTask(otherStaffProfile, 501, TaskType.DAILY_CLEAN, 30, TaskStatus.ASSIGNED, TODAY);
        CleaningTask completedTask = assignTask(staffProfile, 502, TaskType.DEEP_CLEAN, 120, TaskStatus.COMPLETED, TODAY);

        mockMvc.perform(post("/api/staff/tasks/" + otherStaffTask.getId() + "/complete")
                        .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.message").value("You can only complete tasks assigned to you."));

        mockMvc.perform(post("/api/staff/tasks/" + completedTask.getId() + "/complete")
                        .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.message").value("Task is already completed."));
    }


    @Test
    void markTaskCompleteRejectsOffDutyStaff() throws Exception {
        CleaningTask task = assignTask(staffProfile, 503, TaskType.DAILY_CLEAN, 30, TaskStatus.ASSIGNED, TODAY);

        mockMvc.perform(post("/api/staff/tasks/" + task.getId() + "/complete")
                        .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.message").value("You must be on duty to complete tasks."));
    }
    @Test
    void workboardEndpointsAreStaffOnly() throws Exception {
        mockMvc.perform(get("/api/staff/tasks/today")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/staff/tasks/today"))
                .andExpect(status().isUnauthorized());
    }

    private CleaningTask assignTask(
            StaffProfile assignee,
            int roomNumber,
            TaskType taskType,
            int estimatedMinutes,
            TaskStatus taskStatus,
            LocalDate taskDate
    ) {
        Room room = roomRepository.save(Room.builder()
                .roomNumber(roomNumber)
                .roomStatus(RoomStatus.ACTIVE)
                .build());

        CleaningTask task = cleaningTaskRepository.save(CleaningTask.builder()
                .room(room)
                .taskDate(taskDate)
                .shift(morningShift)
                .taskType(taskType)
                .priorityOrder(roomNumber)
                .estimatedMinutes(estimatedMinutes)
                .taskStatus(taskStatus)
                .completedAt(taskStatus == TaskStatus.COMPLETED ? java.time.LocalDateTime.now() : null)
                .build());

        taskAssignmentRepository.save(TaskAssignment.builder()
                .cleaningTask(task)
                .staff(assignee)
                .build());

        return task;
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


