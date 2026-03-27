package com.ibe.housekeeping.allocation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibe.housekeeping.auth.dto.LoginRequest;
import com.ibe.housekeeping.auth.repository.UserRepository;
import com.ibe.housekeeping.allocation.repository.TaskAssignmentRepository;
import com.ibe.housekeeping.common.enums.AvailabilityStatus;
import com.ibe.housekeeping.common.enums.LeaveStatus;
import com.ibe.housekeeping.common.enums.LeaveType;
import com.ibe.housekeeping.common.enums.Role;
import com.ibe.housekeeping.common.enums.RoomStatus;
import com.ibe.housekeeping.common.enums.TaskStatus;
import com.ibe.housekeeping.common.enums.TaskType;
import com.ibe.housekeeping.entity.CleaningTask;
import com.ibe.housekeeping.entity.LeaveRequest;
import com.ibe.housekeeping.entity.Room;
import com.ibe.housekeeping.entity.RoomStay;
import com.ibe.housekeeping.entity.Shift;
import com.ibe.housekeeping.entity.StaffProfile;
import com.ibe.housekeeping.entity.TaskAssignment;
import com.ibe.housekeeping.entity.User;
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
                .andExpect(jsonPath("$.assignments[0].preferredShiftMatched").value(true))
                .andExpect(jsonPath("$.assignments[1].preferredShiftMatched").value(true))
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

        assertThat(refreshedHigherHistorical.getTotalMinutesWorked()).isEqualTo(300);
        assertThat(refreshedLowerHistorical.getTotalMinutesWorked()).isEqualTo(100);

        mockMvc.perform(get("/api/allocation")
                        .header("Authorization", "Bearer " + accessToken)
                        .param("taskDate", TASK_DATE.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary.assignedTasks").value(8))
                .andExpect(jsonPath("$.summary.unassignedTasks").value(0))
                .andExpect(jsonPath("$.assignments.length()").value(8))
                .andExpect(jsonPath("$.assignments[0].staffUsername").isNotEmpty())
                .andExpect(jsonPath("$.unassigned.length()").value(0));

        mockMvc.perform(get("/api/allocation/assignments")
                        .header("Authorization", "Bearer " + accessToken)
                        .param("taskDate", TASK_DATE.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(8))
                .andExpect(jsonPath("$[0].staffUsername").value("staff-b"))
                .andExpect(jsonPath("$[0].staffName").value("Staff B"));
    }

    @Test
    void runAllocationOverridesPreferredShiftWhenNeededToAvoidShortage() throws Exception {
        createStaff("staff-a", "Morning Preferred", morningShift, 10);
        createStaff("staff-b", "Afternoon Preferred", afternoonShift, 20);

        createStay(201, TASK_DATE.minusDays(2), TASK_DATE);
        createStay(202, TASK_DATE.minusDays(2), TASK_DATE);
        createStay(203, TASK_DATE.minusDays(2), TASK_DATE);

        mockMvc.perform(post("/api/allocation/run")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "taskDate":"%s"
                                }
                                """.formatted(TASK_DATE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary.totalTasks").value(3))
                .andExpect(jsonPath("$.summary.assignedTasks").value(3))
                .andExpect(jsonPath("$.summary.unassignedTasks").value(0))
                .andExpect(jsonPath("$.assignments.length()").value(3))
                .andExpect(jsonPath("$.unassigned.length()").value(0));

        List<TaskAssignment> assignments = taskAssignmentRepository.findAllByTaskDate(TASK_DATE);
        assertThat(assignments).hasSize(3);
        assertThat(assignments.stream().map(TaskAssignment::getCleaningTask))
                .allMatch(task -> task.getTaskType() == TaskType.DEEP_CLEAN);
        assertThat(assignments.stream().map(TaskAssignment::getStaff).map(StaffProfile::getFullName))
                .contains("Morning Preferred", "Afternoon Preferred");
        assertThat(assignments.stream()
                .filter(assignment -> !assignment.getCleaningTask().getShift().getId().equals(assignment.getStaff().getPreferredShift().getId()))
                .count()).isGreaterThan(0L);
    }

    @Test
    void runAllocationAssignsExactFitTenCheckoutRoomsAcrossFiveWorkers() throws Exception {
        for (int index = 0; index < 5; index++) {
            Shift preferredShift = index % 2 == 0 ? morningShift : afternoonShift;
            createStaff("staff-fit-" + index, "Fit Staff " + index, preferredShift, index * 10);
        }

        for (int roomNumber = 501; roomNumber <= 510; roomNumber++) {
            createStay(roomNumber, TASK_DATE.minusDays(2), TASK_DATE);
        }

        mockMvc.perform(post("/api/allocation/run")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "taskDate":"%s"
                                }
                                """.formatted(TASK_DATE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary.totalTasks").value(10))
                .andExpect(jsonPath("$.summary.assignedTasks").value(10))
                .andExpect(jsonPath("$.summary.unassignedTasks").value(0))
                .andExpect(jsonPath("$.assignments.length()").value(10))
                .andExpect(jsonPath("$.unassigned.length()").value(0));

        List<TaskAssignment> assignments = taskAssignmentRepository.findAllByTaskDate(TASK_DATE);
        assertThat(assignments).hasSize(10);
        assertThat(assignments.stream().map(TaskAssignment::getCleaningTask))
                .allMatch(task -> task.getTaskType() == TaskType.DEEP_CLEAN);

        assertThat(assignments.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        assignment -> assignment.getStaff().getFullName(),
                        java.util.stream.Collectors.summingInt(assignment -> assignment.getCleaningTask().getEstimatedMinutes())
                )))
                .hasSize(5)
                .allSatisfy((staffName, totalMinutes) -> assertThat(totalMinutes).isEqualTo(240));
    }

    @Test
    void runAllocationLeavesOneCheckoutUnassignedWhenDeepCleanDemandExceedsCapacity() throws Exception {
        for (int index = 0; index < 5; index++) {
            Shift preferredShift = index % 2 == 0 ? morningShift : afternoonShift;
            createStaff("staff-overflow-" + index, "Overflow Staff " + index, preferredShift, 0);
        }

        for (int roomNumber = 601; roomNumber <= 611; roomNumber++) {
            createStay(roomNumber, TASK_DATE.minusDays(2), TASK_DATE);
        }

        mockMvc.perform(post("/api/allocation/run")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "taskDate":"%s"
                                }
                                """.formatted(TASK_DATE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary.totalTasks").value(11))
                .andExpect(jsonPath("$.summary.assignedTasks").value(10))
                .andExpect(jsonPath("$.summary.unassignedTasks").value(1))
                .andExpect(jsonPath("$.assignments.length()").value(10))
                .andExpect(jsonPath("$.unassigned.length()").value(1))
                .andExpect(jsonPath("$.unassigned[0].taskType").value("DEEP_CLEAN"));
    }

    @Test
    void runAllocationUsesHistoricalMinutesAsFairnessTieBreakerWhenPreferenceAlsoMatches() throws Exception {
        StaffProfile lowerHistorical = createStaff("staff-low", "Lower Historical", morningShift, 60);
        StaffProfile higherHistorical = createStaff("staff-high", "Higher Historical", morningShift, 300);
        createStay(701, TASK_DATE.minusDays(2), TASK_DATE);

        mockMvc.perform(post("/api/allocation/run")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "taskDate":"%s"
                                }
                                """.formatted(TASK_DATE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary.totalTasks").value(1))
                .andExpect(jsonPath("$.summary.assignedTasks").value(1))
                .andExpect(jsonPath("$.summary.unassignedTasks").value(0))
                .andExpect(jsonPath("$.assignments[0].staffName").value("Lower Historical"))
                .andExpect(jsonPath("$.assignments[0].preferredShiftMatched").value(true));

        StaffProfile refreshedLowerHistorical = staffProfileRepository.findById(lowerHistorical.getId()).orElseThrow();
        StaffProfile refreshedHigherHistorical = staffProfileRepository.findById(higherHistorical.getId()).orElseThrow();

        assertThat(refreshedLowerHistorical.getTotalMinutesWorked()).isEqualTo(60);
        assertThat(refreshedHigherHistorical.getTotalMinutesWorked()).isEqualTo(300);
    }

    @Test
    void runAllocationLeavesTasksUnassignedWhenDailyCapacityIsExhausted() throws Exception {
        createStaff("staff-a", "Morning Only", morningShift, 0);

        for (int roomNumber = 301; roomNumber <= 317; roomNumber++) {
            createStay(roomNumber, TASK_DATE.minusDays(1), TASK_DATE.plusDays(1));
        }

        mockMvc.perform(post("/api/allocation/run")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "taskDate":"%s"
                                }
                                """.formatted(TASK_DATE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary.totalTasks").value(17))
                .andExpect(jsonPath("$.summary.assignedTasks").value(8))
                .andExpect(jsonPath("$.summary.unassignedTasks").value(9))
                .andExpect(jsonPath("$.unassigned.length()").value(9));

        List<TaskAssignment> assignments = taskAssignmentRepository.findAllByTaskDate(TASK_DATE);
        assertThat(assignments).hasSize(8);
        assertThat(assignments.stream()
                .map(TaskAssignment::getCleaningTask)
                .mapToInt(CleaningTask::getEstimatedMinutes)
                .sum()).isEqualTo(240);
        assertThat(assignments.stream()
                .map(TaskAssignment::getCleaningTask)
                .map(task -> task.getShift().getId())
                .distinct()).containsExactly(morningShift.getId());
    }

    @Test
    void runAllocationUsesMorningDeepCleanBucketForDailyTasksAfterMorningGeneralBucketFills() throws Exception {
        createStaff("staff-morning", "Morning Preferred", morningShift, 0);

        for (int roomNumber = 801; roomNumber <= 806; roomNumber++) {
            createStay(roomNumber, TASK_DATE.minusDays(1), TASK_DATE.plusDays(1));
        }

        mockMvc.perform(post("/api/allocation/run")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "taskDate":"%s"
                                }
                                """.formatted(TASK_DATE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary.totalTasks").value(6))
                .andExpect(jsonPath("$.summary.assignedTasks").value(6))
                .andExpect(jsonPath("$.summary.unassignedTasks").value(0))
                .andExpect(jsonPath("$.assignments.length()").value(6))
                .andExpect(jsonPath("$.unassigned.length()").value(0));

        List<TaskAssignment> assignments = taskAssignmentRepository.findAllByTaskDate(TASK_DATE);
        assertThat(assignments).hasSize(6);
        assertThat(assignments.stream().map(TaskAssignment::getCleaningTask))
                .allMatch(task -> task.getTaskType() == TaskType.DAILY_CLEAN);
        assertThat(assignments.stream()
                .map(TaskAssignment::getStaff)
                .map(StaffProfile::getFullName))
                .containsOnly("Morning Preferred");
        assertThat(assignments.stream()
                .map(TaskAssignment::getCleaningTask)
                .allMatch(task -> morningShift.getId().equals(task.getShift().getId()))).isTrue();
    }

    @Test
    void getAllocationKeepsUnassignedTasksVisibleAfterRun() throws Exception {
        StaffProfile availableStaff = createStaff("staff-a", "Available Staff", morningShift, 0);
        createLeave(availableStaff, TASK_DATE.minusDays(1), TASK_DATE.plusDays(1));
        createStay(401, TASK_DATE.minusDays(1), TASK_DATE.plusDays(1));

        mockMvc.perform(post("/api/allocation/run")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "taskDate":"%s"
                                }
                                """.formatted(TASK_DATE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary.assignedTasks").value(0))
                .andExpect(jsonPath("$.summary.unassignedTasks").value(1))
                .andExpect(jsonPath("$.unassigned[0].taskType").value("DAILY_CLEAN"));

        mockMvc.perform(get("/api/allocation")
                        .header("Authorization", "Bearer " + accessToken)
                        .param("taskDate", TASK_DATE.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary.assignedTasks").value(0))
                .andExpect(jsonPath("$.summary.unassignedTasks").value(1))
                .andExpect(jsonPath("$.assignments.length()").value(0))
                .andExpect(jsonPath("$.unassigned.length()").value(1));
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
                .preferredShift(shift)
                .availabilityStatus(AvailabilityStatus.OFF_DUTY)
                .totalMinutesWorked(totalMinutesWorked)
                .build());
    }

    private LeaveRequest createLeave(StaffProfile staff, LocalDate leaveStartDate, LocalDate leaveEndDate) {
        return leaveRequestRepository.save(LeaveRequest.builder()
                .staff(staff)
                .leaveStartDate(leaveStartDate)
                .leaveEndDate(leaveEndDate)
                .leaveType(LeaveType.SICK)
                .reason("Planned leave")
                .status(LeaveStatus.APPROVED)
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
