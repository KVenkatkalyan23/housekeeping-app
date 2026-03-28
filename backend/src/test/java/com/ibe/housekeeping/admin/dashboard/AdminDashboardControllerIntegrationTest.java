package com.ibe.housekeeping.admin.dashboard;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibe.housekeeping.auth.dto.LoginRequest;
import com.ibe.housekeeping.auth.repository.UserRepository;
import com.ibe.housekeeping.common.enums.AvailabilityStatus;
import com.ibe.housekeeping.common.enums.Role;
import com.ibe.housekeeping.common.enums.RoomStatus;
import com.ibe.housekeeping.common.enums.TaskStatus;
import com.ibe.housekeeping.common.enums.TaskType;
import com.ibe.housekeeping.entity.CleaningTask;
import com.ibe.housekeeping.entity.Room;
import com.ibe.housekeeping.entity.RoomStay;
import com.ibe.housekeeping.entity.Shift;
import com.ibe.housekeeping.entity.StaffProfile;
import com.ibe.housekeeping.entity.User;
import com.ibe.housekeeping.room.repository.RoomRepository;
import com.ibe.housekeeping.roomstay.repository.RoomStayRepository;
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
class AdminDashboardControllerIntegrationTest {

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
    private RoomStayRepository roomStayRepository;

    @Autowired
    private CleaningTaskRepository cleaningTaskRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String adminToken;
    private String staffToken;
    private Shift morningShift;
    private Shift eveningShift;

    @BeforeEach
    void setUp() throws Exception {
        cleaningTaskRepository.deleteAll();
        roomStayRepository.deleteAll();
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
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(15, 0))
                .durationMinutes(360)
                .build());

        eveningShift = shiftRepository.save(Shift.builder()
                .shiftCode("EVE")
                .shiftName("Evening")
                .startTime(LocalTime.of(15, 0))
                .endTime(LocalTime.of(21, 0))
                .durationMinutes(360)
                .build());

        staffProfileRepository.save(StaffProfile.builder()
                .user(staff)
                .fullName("Staff User")
                .email("staff-user@housekeeping.local")
                .phone("9999999999")
                .preferredShift(morningShift)
                .availabilityStatus(AvailabilityStatus.ON_DUTY)
                .build());

        User supportStaff = userRepository.save(User.builder()
                .username("support-user")
                .password(passwordEncoder.encode("password123"))
                .role(Role.STAFF)
                .build());

        staffProfileRepository.save(StaffProfile.builder()
                .user(supportStaff)
                .fullName("Support User")
                .email("support-user@housekeeping.local")
                .phone("8888888888")
                .preferredShift(eveningShift)
                .availabilityStatus(AvailabilityStatus.ON_DUTY)
                .build());

        seedRoomsAndTasks(FIXED_TODAY);
        seedYesterdayTasks(FIXED_TODAY.minusDays(1));

        adminToken = authenticate(admin.getUsername(), "password123");
        staffToken = authenticate(staff.getUsername(), "password123");
    }

    @Test
    void adminDashboardReturnsAggregatedMetricsForAdmin() throws Exception {
        mockMvc.perform(get("/api/admin/dashboard")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shortfallAlert.shortfallPercent").isNumber())
                .andExpect(jsonPath("$.shortfallAlert.shortfallMessage").isString())
                .andExpect(jsonPath("$.inventoryStatus.totalRooms").value(4))
                .andExpect(jsonPath("$.inventoryStatus.occupiedRooms").value(2))
                .andExpect(jsonPath("$.inventoryStatus.vacantRooms").value(2))
                .andExpect(jsonPath("$.workforceEfficiency.utilizationPercent").value(58))
                .andExpect(jsonPath("$.slaPerformance.completionRate").value(50))
                .andExpect(jsonPath("$.capacityVsWorkload.availableTotalHours").value(12.0))
                .andExpect(jsonPath("$.capacityVsWorkload.requiredTotalHours").value(7.0))
                .andExpect(jsonPath("$.currentAdmin.displayName").value("Admin User"))
                .andExpect(jsonPath("$.currentAdmin.roleLabel").value("MASTER ADMIN"));
    }

    @Test
    void adminDashboardIsForbiddenForStaff() throws Exception {
        mockMvc.perform(get("/api/admin/dashboard")
                        .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.message").value("Access denied"));
    }

    @Test
    void adminDashboardRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/admin/dashboard"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.message").value("Authentication required"));
    }

    private void seedRoomsAndTasks(LocalDate taskDate) {
        Room room101 = roomRepository.save(Room.builder()
                .roomNumber(101)
                .roomStatus(RoomStatus.ACTIVE)
                .build());
        Room room102 = roomRepository.save(Room.builder()
                .roomNumber(102)
                .roomStatus(RoomStatus.ACTIVE)
                .build());
        Room room103 = roomRepository.save(Room.builder()
                .roomNumber(103)
                .roomStatus(RoomStatus.ACTIVE)
                .build());

        roomStayRepository.save(RoomStay.builder()
                .room(room101)
                .checkInDate(taskDate.minusDays(1))
                .checkOutDate(taskDate.plusDays(1))
                .build());
        roomStayRepository.save(RoomStay.builder()
                .room(room102)
                .checkInDate(taskDate)
                .checkOutDate(taskDate.plusDays(2))
                .build());

        cleaningTaskRepository.save(CleaningTask.builder()
                .room(room101)
                .taskDate(taskDate)
                .shift(morningShift)
                .taskType(TaskType.DAILY_CLEAN)
                .priorityOrder(1)
                .estimatedMinutes(180)
                .taskStatus(TaskStatus.COMPLETED)
                .build());

        cleaningTaskRepository.save(CleaningTask.builder()
                .room(room102)
                .taskDate(taskDate)
                .shift(eveningShift)
                .taskType(TaskType.DEEP_CLEAN)
                .priorityOrder(2)
                .estimatedMinutes(240)
                .taskStatus(TaskStatus.ASSIGNED)
                .build());
    }

    private void seedYesterdayTasks(LocalDate taskDate) {
        Room yesterdayRoom = roomRepository.save(Room.builder()
                .roomNumber(201)
                .roomStatus(RoomStatus.ACTIVE)
                .build());

        cleaningTaskRepository.save(CleaningTask.builder()
                .room(yesterdayRoom)
                .taskDate(taskDate)
                .shift(morningShift)
                .taskType(TaskType.DAILY_CLEAN)
                .priorityOrder(1)
                .estimatedMinutes(120)
                .taskStatus(TaskStatus.COMPLETED)
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
