package com.ibe.housekeeping.allocation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibe.housekeeping.auth.dto.LoginRequest;
import com.ibe.housekeeping.auth.repository.UserRepository;
import com.ibe.housekeeping.allocation.repository.TaskAssignmentRepository;
import com.ibe.housekeeping.common.enums.AvailabilityStatus;
import com.ibe.housekeeping.common.enums.Role;
import com.ibe.housekeeping.common.enums.RoomStatus;
import com.ibe.housekeeping.common.enums.TaskStatus;
import com.ibe.housekeeping.entity.CleaningTask;
import com.ibe.housekeeping.entity.Room;
import com.ibe.housekeeping.entity.Shift;
import com.ibe.housekeeping.entity.StaffProfile;
import com.ibe.housekeeping.entity.User;
import com.ibe.housekeeping.entity.RoomStay;
import com.ibe.housekeeping.leave.repository.LeaveRequestRepository;
import com.ibe.housekeeping.room.repository.RoomRepository;
import com.ibe.housekeeping.roomstay.repository.RoomStayRepository;
import com.ibe.housekeeping.shift.repository.ShiftRepository;
import com.ibe.housekeeping.staff.repository.StaffProfileRepository;
import com.ibe.housekeeping.task.repository.CleaningTaskRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
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
class TaskAllocationControllerIntegrationTest {

    private static final LocalDate TASK_DATE = LocalDate.of(2026, 3, 26);

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ShiftRepository shiftRepository;

    @Autowired
    private StaffProfileRepository staffProfileRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private RoomStayRepository roomStayRepository;

    @Autowired
    private CleaningTaskRepository cleaningTaskRepository;

    @Autowired
    private TaskAssignmentRepository taskAssignmentRepository;

    @Autowired
    private LeaveRequestRepository leaveRequestRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String accessToken;
    private Shift morningShift;
    private Shift afternoonShift;

    @BeforeEach
    void setUp() throws Exception {
        taskAssignmentRepository.deleteAll();
        cleaningTaskRepository.deleteAll();
        roomStayRepository.deleteAll();
        leaveRequestRepository.deleteAll();
        roomRepository.deleteAll();
        staffProfileRepository.deleteAll();
        shiftRepository.deleteAll();
        userRepository.deleteAll();

        userRepository.save(User.builder()
                .username("admin")
                .password(passwordEncoder.encode("password123"))
                .role(Role.ADMIN)
                .build());

        accessToken = authenticate("admin", "password123");

        morningShift = shiftRepository.save(Shift.builder()
                .shiftCode("MORN")
                .shiftName("Morning Shift")
                .startTime(LocalTime.of(8, 0))
                .endTime(LocalTime.of(12, 0))
                .durationMinutes(240)
                .build());

        afternoonShift = shiftRepository.save(Shift.builder()
                .shiftCode("AFT")
                .shiftName("Afternoon Shift")
                .startTime(LocalTime.of(13, 0))
                .endTime(LocalTime.of(17, 0))
                .durationMinutes(240)
                .build());
    }

    @Test
    void runAllocationAssignsByPriorityFairnessAndCapacity() throws Exception {
        StaffProfile higherHistorical = createStaff("staff-a", "Staff A", morningShift, 300);
        StaffProfile lowerHistorical = createStaff("staff-b", "Staff B", afternoonShift, 100);
        createStay(101, TASK_DATE.minusDays(2), TASK_DATE);
        createStay(102, TASK_DATE.minusDays(1), TASK_DATE);
        createStay(103, TASK_DATE.minusDays(1), TASK_DATE.plusDays(1));
        createStay(104, TASK_DATE.minusDays(2), TASK_DATE.plusDays(2));
        createStay(105, TASK_DATE.minusDays(3), TASK_DATE.plusDays(1));
        createStay(106, TASK_DATE.minusDays(2), TASK_DATE.plusDays(3));
        createStay(107, TASK_DATE.minusDays(4), TASK_DATE.plusDays(1));
        createRoom(108);

        mockMvc.perform(post("/api/allocation/run")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "taskDate":"%s"
                                }
                                """.formatted(TASK_DATE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary.taskDate").value(TASK_DATE.toString()))
                .andExpect(jsonPath("$.summary.totalTasks").value(8))
                .andExpect(jsonPath("$.summary.assignedTasks").value(8))
                .andExpect(jsonPath("$.summary.unassignedTasks").value(0))
                .andExpect(jsonPath("$.assignments.length()").value(8))
                .andExpect(jsonPath("$.assignments[0].taskType").value("DEEP_CLEAN"))
                .andExpect(jsonPath("$.assignments[0].staffName").value("Staff B"))
                .andExpect(jsonPath("$.assignments[1].taskType").value("DEEP_CLEAN"))
                .andExpect(jsonPath("$.assignments[1].staffName").value("Staff A"))
                .andExpect(jsonPath("$.assignments[0].shiftName").value("Afternoon Shift"))
                .andExpect(jsonPath("$.assignments[1].shiftName").value("Morning Shift"))
                .andExpect(jsonPath("$.unassigned.length()").value(0));

        List<CleaningTask> savedTasks = cleaningTaskRepository.findAllByTaskDateOrderByPriorityOrderAscRoomRoomNumberAsc(TASK_DATE);
        long assignedCount = savedTasks.stream().filter(task -> task.getShift() != null).count();
        long stillUnassignedCount = savedTasks.stream().filter(task -> task.getShift() == null).count();

        assertThat(assignedCount).isEqualTo(8);
        assertThat(stillUnassignedCount).isZero();
        assertThat(savedTasks.stream().map(task -> task.getShift().getId()).distinct()).containsExactlyInAnyOrder(
                morningShift.getId(),
                afternoonShift.getId()
        );
        assertThat(savedTasks.stream()
                .filter(task -> task.getShift() != null)
                .allMatch(task -> task.getTaskStatus() == TaskStatus.ASSIGNED)).isTrue();

        StaffProfile refreshedHigherHistorical = staffProfileRepository.findById(higherHistorical.getId()).orElseThrow();
        StaffProfile refreshedLowerHistorical = staffProfileRepository.findById(lowerHistorical.getId()).orElseThrow();

        assertThat(refreshedHigherHistorical.getTotalMinutesWorked()).isEqualTo(495);
        assertThat(refreshedLowerHistorical.getTotalMinutesWorked()).isEqualTo(310);

        mockMvc.perform(get("/api/allocation")
                        .header("Authorization", "Bearer " + accessToken)
                        .param("taskDate", TASK_DATE.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary.assignedTasks").value(8))
                .andExpect(jsonPath("$.summary.unassignedTasks").value(0))
                .andExpect(jsonPath("$.assignments.length()").value(8))
                .andExpect(jsonPath("$.unassigned.length()").value(0));
    }

    private StaffProfile createStaff(String username, String fullName, Shift shift, int totalMinutesWorked) {
        User user = userRepository.save(User.builder()
                .username(username)
                .password(passwordEncoder.encode("password123"))
                .role(Role.STAFF)
                .build());

        return staffProfileRepository.save(StaffProfile.builder()
                .user(user)
                .fullName(fullName)
                .email(username + "@housekeeping.local")
                .phone("+15550000000")
                .currentShift(shift)
                .availabilityStatus(AvailabilityStatus.OFF_DUTY)
                .totalMinutesWorked(totalMinutesWorked)
                .build());
    }

    private Room createRoom(int roomNumber) {
        return roomRepository.save(Room.builder()
                .roomNumber(roomNumber)
                .roomStatus(RoomStatus.ACTIVE)
                .build());
    }

    private RoomStay createStay(int roomNumber, LocalDate checkInDate, LocalDate checkOutDate) {
        Room room = createRoom(roomNumber);
        return roomStayRepository.save(RoomStay.builder()
                .room(room)
                .checkInDate(checkInDate)
                .checkOutDate(checkOutDate)
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
