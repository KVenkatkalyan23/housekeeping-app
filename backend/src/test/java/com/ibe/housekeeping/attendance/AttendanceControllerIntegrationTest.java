package com.ibe.housekeeping.attendance;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibe.housekeeping.allocation.repository.TaskAssignmentRepository;
import com.ibe.housekeeping.attendance.repository.AttendanceRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(FixedClockTestConfig.class)
class AttendanceControllerIntegrationTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 3, 27);

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
    private TaskAssignmentRepository taskAssignmentRepository;

    @Autowired
    private CleaningTaskRepository cleaningTaskRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private RoomStayRepository roomStayRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String staffToken;
    private String otherStaffToken;
    private StaffProfile staffProfile;
    private StaffProfile otherStaffProfile;
    private Shift morningShift;

    @BeforeEach
    void setUp() throws Exception {
        taskAssignmentRepository.deleteAll();
        cleaningTaskRepository.deleteAll();
        attendanceRepository.deleteAll();
        roomStayRepository.deleteAll();
        roomRepository.deleteAll();
        staffProfileRepository.deleteAll();
        shiftRepository.deleteAll();
        userRepository.deleteAll();

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
        shiftRepository.save(Shift.builder()
                .shiftCode("AFT")
                .shiftName("Afternoon Shift")
                .startTime(LocalTime.of(13, 0))
                .endTime(LocalTime.of(17, 0))
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
    }

    @Test
    void clockOutDetachesAndReassignsUnfinishedTasksToOtherOnDutyStaff() throws Exception {
        mockMvc.perform(post("/api/attendance/clock-in")
                        .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/attendance/clock-in")
                        .header("Authorization", "Bearer " + otherStaffToken))
                .andExpect(status().isOk());

        assignTask(staffProfile, 401, TaskStatus.ASSIGNED);

        mockMvc.perform(post("/api/attendance/clock-out")
                        .header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.staffId").value(staffProfile.getId().toString()))
                .andExpect(jsonPath("$.availabilityStatus").value("OFF_DUTY"));

        assertThat(taskAssignmentRepository.findAllByTaskDate(TODAY))
                .hasSize(1)
                .allSatisfy(assignment -> assertThat(assignment.getStaff().getId()).isEqualTo(otherStaffProfile.getId()));
    }

    @Test
    void clockOutCanReassignToOffDutyStaffWhoDidNotClockOutToday() throws Exception {
        mockMvc.perform(post("/api/attendance/clock-in")
                        .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isOk());

        assignTask(staffProfile, 402, TaskStatus.ASSIGNED);

        mockMvc.perform(post("/api/attendance/clock-out")
                        .header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        assertThat(taskAssignmentRepository.findAllByTaskDate(TODAY))
                .hasSize(1)
                .allSatisfy(assignment -> assertThat(assignment.getStaff().getId()).isEqualTo(otherStaffProfile.getId()));
    }

    @Test
    void clockOutLeavesTasksUnassignedWhenAllOtherStaffAlreadyClockedOutToday() throws Exception {
        mockMvc.perform(post("/api/attendance/clock-in")
                        .header("Authorization", "Bearer " + otherStaffToken))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/attendance/clock-out")
                        .header("Authorization", "Bearer " + otherStaffToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/attendance/clock-in")
                        .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isOk());

        assignTask(staffProfile, 403, TaskStatus.ASSIGNED);

        mockMvc.perform(post("/api/attendance/clock-out")
                        .header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        assertThat(taskAssignmentRepository.findAllByTaskDate(TODAY)).isEmpty();
        assertThat(cleaningTaskRepository.findAllByTaskDateOrderByPriorityOrderAscRoomRoomNumberAsc(TODAY))
                .hasSize(1)
                .allSatisfy(task -> assertThat(task.getTaskStatus()).isEqualTo(TaskStatus.PENDING));
    }

    @Test
    void clockOutDetachesPendingAndAssignedButKeepsCompletedAndInProgressTasks() throws Exception {
        mockMvc.perform(post("/api/attendance/clock-in")
                        .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/attendance/clock-in")
                        .header("Authorization", "Bearer " + otherStaffToken))
                .andExpect(status().isOk());

        assignTask(staffProfile, 404, TaskStatus.PENDING);
        assignTask(staffProfile, 405, TaskStatus.ASSIGNED);
        assignTask(staffProfile, 406, TaskStatus.IN_PROGRESS);
        assignTask(staffProfile, 407, TaskStatus.COMPLETED);

        mockMvc.perform(post("/api/attendance/clock-out")
                        .header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        assertThat(taskAssignmentRepository.findAllByTaskDate(TODAY))
                .hasSize(4)
                .anySatisfy(assignment -> {
                    assertThat(assignment.getCleaningTask().getRoom().getRoomNumber()).isEqualTo(404);
                    assertThat(assignment.getStaff().getId()).isEqualTo(otherStaffProfile.getId());
                })
                .anySatisfy(assignment -> {
                    assertThat(assignment.getCleaningTask().getRoom().getRoomNumber()).isEqualTo(405);
                    assertThat(assignment.getStaff().getId()).isEqualTo(otherStaffProfile.getId());
                })
                .anySatisfy(assignment -> {
                    assertThat(assignment.getCleaningTask().getRoom().getRoomNumber()).isEqualTo(406);
                    assertThat(assignment.getStaff().getId()).isEqualTo(staffProfile.getId());
                })
                .anySatisfy(assignment -> {
                    assertThat(assignment.getCleaningTask().getRoom().getRoomNumber()).isEqualTo(407);
                    assertThat(assignment.getStaff().getId()).isEqualTo(staffProfile.getId());
                });
    }

    @Test
    void clockOutKeepsOtherStaffAssignmentsAndAddsRelocatedTasksOnTop() throws Exception {
        mockMvc.perform(post("/api/attendance/clock-in")
                        .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/attendance/clock-in")
                        .header("Authorization", "Bearer " + otherStaffToken))
                .andExpect(status().isOk());

        assignTask(staffProfile, 408, TaskStatus.ASSIGNED);
        assignTask(otherStaffProfile, 409, TaskStatus.ASSIGNED);

        mockMvc.perform(post("/api/attendance/clock-out")
                        .header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        assertThat(taskAssignmentRepository.findAllByTaskDate(TODAY))
                .hasSize(2)
                .allSatisfy(assignment -> assertThat(assignment.getStaff().getId()).isEqualTo(otherStaffProfile.getId()));
    }

    private void assignTask(StaffProfile assignee, int roomNumber, TaskStatus taskStatus) {
        Room room = roomRepository.save(Room.builder()
                .roomNumber(roomNumber)
                .roomStatus(RoomStatus.ACTIVE)
                .build());

        CleaningTask task = cleaningTaskRepository.save(CleaningTask.builder()
                .room(room)
                .taskDate(TODAY)
                .shift(morningShift)
                .taskType(TaskType.DAILY_CLEAN)
                .priorityOrder(roomNumber)
                .estimatedMinutes(30)
                .taskStatus(taskStatus)
                .build());

        taskAssignmentRepository.save(TaskAssignment.builder()
                .cleaningTask(task)
                .staff(assignee)
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
