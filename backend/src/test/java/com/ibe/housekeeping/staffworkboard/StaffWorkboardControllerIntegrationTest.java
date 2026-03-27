package com.ibe.housekeeping.staffworkboard;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibe.housekeeping.auth.dto.LoginRequest;
import com.ibe.housekeeping.auth.repository.UserRepository;
import com.ibe.housekeeping.allocation.repository.TaskAssignmentRepository;
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
import java.time.LocalDateTime;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class StaffWorkboardControllerIntegrationTest {

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

    private String staffAccessToken;
    private String adminAccessToken;
    private StaffProfile staffProfile;
    private Shift morningShift;

    @BeforeEach
    void setUp() throws Exception {
        taskAssignmentRepository.deleteAll();
        cleaningTaskRepository.deleteAll();
        roomRepository.deleteAll();
        staffProfileRepository.deleteAll();
        shiftRepository.deleteAll();
        userRepository.deleteAll();

        morningShift = shiftRepository.save(Shift.builder()
                .shiftCode("MORN")
                .shiftName("Morning Shift")
                .startTime(LocalTime.of(8, 0))
                .endTime(LocalTime.of(12, 0))
                .durationMinutes(240)
                .build());

        User staffUser = userRepository.save(User.builder()
                .username("staff")
                .password(passwordEncoder.encode("password123"))
                .role(Role.STAFF)
                .build());

        userRepository.save(User.builder()
                .username("admin")
                .password(passwordEncoder.encode("password123"))
                .role(Role.ADMIN)
                .build());

        staffProfile = staffProfileRepository.save(StaffProfile.builder()
                .user(staffUser)
                .fullName("Staff User")
                .email("staff@housekeeping.local")
                .phone("+15550000000")
                .preferredShift(morningShift)
                .availabilityStatus(AvailabilityStatus.OFF_DUTY)
                .totalMinutesWorked(0)
                .build());

        staffAccessToken = authenticate("staff", "password123");
        adminAccessToken = authenticate("admin", "password123");
    }

    @Test
    void getTodayAssignedTasksAndWorkloadReturnsOnlyLoggedInStaffTasksForToday() throws Exception {
        LocalDate today = LocalDate.now();

        Room firstRoom = createRoom(402);
        Room secondRoom = createRoom(305);
        Room otherRoom = createRoom(512);

        CleaningTask assignedTask = cleaningTaskRepository.save(CleaningTask.builder()
                .room(firstRoom)
                .taskDate(today)
                .shift(morningShift)
                .taskType(TaskType.DAILY_CLEAN)
                .priorityOrder(1)
                .estimatedMinutes(30)
                .taskStatus(TaskStatus.ASSIGNED)
                .build());

        CleaningTask completedTask = cleaningTaskRepository.save(CleaningTask.builder()
                .room(secondRoom)
                .taskDate(today)
                .shift(morningShift)
                .taskType(TaskType.DEEP_CLEAN)
                .priorityOrder(2)
                .estimatedMinutes(120)
                .taskStatus(TaskStatus.COMPLETED)
                .completedAt(LocalDateTime.of(2026, 3, 27, 14, 15))
                .build());

        User otherUser = userRepository.save(User.builder()
                .username("other-staff")
                .password(passwordEncoder.encode("password123"))
                .role(Role.STAFF)
                .build());

        StaffProfile otherStaff = staffProfileRepository.save(StaffProfile.builder()
                .user(otherUser)
                .fullName("Other Staff")
                .email("other@housekeeping.local")
                .phone("+15550000001")
                .preferredShift(morningShift)
                .availabilityStatus(AvailabilityStatus.OFF_DUTY)
                .totalMinutesWorked(0)
                .build());

        CleaningTask otherStaffTask = cleaningTaskRepository.save(CleaningTask.builder()
                .room(otherRoom)
                .taskDate(today)
                .shift(morningShift)
                .taskType(TaskType.VACANT_CLEAN)
                .priorityOrder(3)
                .estimatedMinutes(15)
                .taskStatus(TaskStatus.ASSIGNED)
                .build());

        CleaningTask previousDayTask = cleaningTaskRepository.save(CleaningTask.builder()
                .room(createRoom(610))
                .taskDate(today.minusDays(1))
                .shift(morningShift)
                .taskType(TaskType.DAILY_CLEAN)
                .priorityOrder(4)
                .estimatedMinutes(30)
                .taskStatus(TaskStatus.ASSIGNED)
                .build());

        taskAssignmentRepository.save(TaskAssignment.builder()
                .cleaningTask(assignedTask)
                .staff(staffProfile)
                .build());
        taskAssignmentRepository.save(TaskAssignment.builder()
                .cleaningTask(completedTask)
                .staff(staffProfile)
                .build());
        taskAssignmentRepository.save(TaskAssignment.builder()
                .cleaningTask(otherStaffTask)
                .staff(otherStaff)
                .build());
        taskAssignmentRepository.save(TaskAssignment.builder()
                .cleaningTask(previousDayTask)
                .staff(staffProfile)
                .build());

        mockMvc.perform(get("/api/staff/tasks/today")
                        .header("Authorization", "Bearer " + staffAccessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.date").value(today.toString()))
                .andExpect(jsonPath("$.tasks.length()").value(2))
                .andExpect(jsonPath("$.tasks[0].roomNumber").value(402))
                .andExpect(jsonPath("$.tasks[0].taskType").value("DAILY_CLEAN"))
                .andExpect(jsonPath("$.tasks[0].taskStatus").value("ASSIGNED"))
                .andExpect(jsonPath("$.tasks[0].estimatedMinutes").value(30))
                .andExpect(jsonPath("$.tasks[0].shiftCode").value("MORN"))
                .andExpect(jsonPath("$.tasks[1].roomNumber").value(305))
                .andExpect(jsonPath("$.tasks[1].taskStatus").value("COMPLETED"))
                .andExpect(jsonPath("$.tasks[1].completedAt").value("2026-03-27T14:15:00"));

        mockMvc.perform(get("/api/staff/tasks/workload")
                        .header("Authorization", "Bearer " + staffAccessToken))
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
    void adminCannotAccessStaffWorkboardEndpoints() throws Exception {
        mockMvc.perform(get("/api/staff/tasks/today")
                        .header("Authorization", "Bearer " + adminAccessToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/staff/tasks/workload")
                        .header("Authorization", "Bearer " + adminAccessToken))
                .andExpect(status().isForbidden());
    }

    private Room createRoom(int roomNumber) {
        return roomRepository.save(Room.builder()
                .roomNumber(roomNumber)
                .roomStatus(RoomStatus.ACTIVE)
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
