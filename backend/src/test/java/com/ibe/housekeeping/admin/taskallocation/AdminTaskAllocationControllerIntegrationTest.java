package com.ibe.housekeeping.admin.taskallocation;

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
import com.ibe.housekeeping.support.FixedClockTestConfig;
import com.ibe.housekeeping.task.repository.CleaningTaskRepository;
import java.time.LocalDate;
import java.time.LocalTime;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(FixedClockTestConfig.class)
class AdminTaskAllocationControllerIntegrationTest {

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
    private PasswordEncoder passwordEncoder;

    private String adminToken;
    private String staffToken;
    private Shift morningShift;
    private Shift afternoonShift;
    private StaffProfile elena;
    private StaffProfile marcus;

    @BeforeEach
    void setUp() throws Exception {
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

        elena = createStaff("elena-user", "Elena Rodriguez", morningShift);
        marcus = createStaff("marcus-user", "Marcus Chen", afternoonShift);
        createStaff("staff-user-profile", "Staff User", morningShift);

        seedTasks();

        adminToken = authenticate(admin.getUsername(), "password123");
        staffToken = authenticate(staff.getUsername(), "password123");
    }

    @Test
    void allocationSummaryReturnsAggregatedCardDataForAdmin() throws Exception {
        mockMvc.perform(get("/api/admin/tasks/allocation/summary")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("taskDate", FIXED_TODAY.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taskDate").value("2026-03-27"))
                .andExpect(jsonPath("$.totalActiveTasks").value(4))
                .andExpect(jsonPath("$.inProgressCount").value(1))
                .andExpect(jsonPath("$.checkoutTaskCount").value(2))
                .andExpect(jsonPath("$.checkoutAssignedCount").value(1))
                .andExpect(jsonPath("$.checkoutPendingCount").value(1))
                .andExpect(jsonPath("$.dailyTaskCount").value(1))
                .andExpect(jsonPath("$.dailyAssignedCount").value(1))
                .andExpect(jsonPath("$.dailyPendingCount").value(0))
                .andExpect(jsonPath("$.vacantTaskCount").value(1))
                .andExpect(jsonPath("$.vacantAssignedCount").value(1))
                .andExpect(jsonPath("$.vacantPendingCount").value(0));
    }

    @Test
    void allocationListSupportsSearchFiltersAndPagination() throws Exception {
        mockMvc.perform(get("/api/admin/tasks/allocation")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("taskDate", FIXED_TODAY.toString())
                        .param("search", "elena")
                        .param("taskType", "DEEP_CLEAN")
                        .param("status", "ASSIGNED")
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(5))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].roomNumber").value("402"))
                .andExpect(jsonPath("$.items[0].assignedStaffName").value("Elena Rodriguez"))
                .andExpect(jsonPath("$.items[0].priorityLabel").value("HIGH"))
                .andExpect(jsonPath("$.items[0].shiftName").value("Morning"));
    }

    @Test
    void allocationListReturnsUnassignedTasksAndAppliesPagination() throws Exception {
        mockMvc.perform(get("/api/admin/tasks/allocation")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("taskDate", FIXED_TODAY.toString())
                        .param("page", "2")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(2))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.totalElements").value(5))
                .andExpect(jsonPath("$.totalPages").value(3))
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].roomNumber").value("710"))
                .andExpect(jsonPath("$.items[0].assignedStaffName").isEmpty());
    }

    @Test
    void allocationEndpointsRejectInvalidFiltersAndProtectAccess() throws Exception {
        mockMvc.perform(get("/api/admin/tasks/allocation")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("taskType", "UNKNOWN"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.message").value("taskType must be one of DEEP_CLEAN, DAILY_CLEAN, or VACANT_CLEAN."));

        mockMvc.perform(get("/api/admin/tasks/allocation")
                        .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.message").value("Access denied"));

        mockMvc.perform(get("/api/admin/tasks/allocation/summary"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.message").value("Authentication required"));
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
        Room room512 = roomRepository.save(Room.builder().roomNumber(512).roomStatus(RoomStatus.ACTIVE).build());
        Room room108 = roomRepository.save(Room.builder().roomNumber(108).roomStatus(RoomStatus.ACTIVE).build());
        Room room612 = roomRepository.save(Room.builder().roomNumber(612).roomStatus(RoomStatus.ACTIVE).build());
        Room room710 = roomRepository.save(Room.builder().roomNumber(710).roomStatus(RoomStatus.ACTIVE).build());

        CleaningTask assignedCheckout = cleaningTaskRepository.save(CleaningTask.builder()
                .room(room402)
                .taskDate(FIXED_TODAY)
                .shift(morningShift)
                .taskType(TaskType.DEEP_CLEAN)
                .priorityOrder(1)
                .estimatedMinutes(120)
                .taskStatus(TaskStatus.ASSIGNED)
                .build());

        CleaningTask inProgressDaily = cleaningTaskRepository.save(CleaningTask.builder()
                .room(room512)
                .taskDate(FIXED_TODAY)
                .shift(afternoonShift)
                .taskType(TaskType.DAILY_CLEAN)
                .priorityOrder(2)
                .estimatedMinutes(30)
                .taskStatus(TaskStatus.IN_PROGRESS)
                .build());

        CleaningTask completedVacant = cleaningTaskRepository.save(CleaningTask.builder()
                .room(room108)
                .taskDate(FIXED_TODAY)
                .shift(morningShift)
                .taskType(TaskType.VACANT_CLEAN)
                .priorityOrder(3)
                .estimatedMinutes(15)
                .taskStatus(TaskStatus.COMPLETED)
                .build());

        CleaningTask activeVacant = cleaningTaskRepository.save(CleaningTask.builder()
                .room(room612)
                .taskDate(FIXED_TODAY)
                .shift(afternoonShift)
                .taskType(TaskType.VACANT_CLEAN)
                .priorityOrder(4)
                .estimatedMinutes(15)
                .taskStatus(TaskStatus.ASSIGNED)
                .build());

        cleaningTaskRepository.save(CleaningTask.builder()
                .room(room710)
                .taskDate(FIXED_TODAY)
                .taskType(TaskType.DEEP_CLEAN)
                .priorityOrder(5)
                .estimatedMinutes(120)
                .taskStatus(TaskStatus.PENDING)
                .build());

        taskAssignmentRepository.save(TaskAssignment.builder()
                .cleaningTask(assignedCheckout)
                .staff(elena)
                .build());

        taskAssignmentRepository.save(TaskAssignment.builder()
                .cleaningTask(inProgressDaily)
                .staff(marcus)
                .build());

        taskAssignmentRepository.save(TaskAssignment.builder()
                .cleaningTask(completedVacant)
                .staff(elena)
                .build());

        taskAssignmentRepository.save(TaskAssignment.builder()
                .cleaningTask(activeVacant)
                .staff(marcus)
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



