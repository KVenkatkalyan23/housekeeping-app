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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ReallocationShiftWindowIntegrationTest {

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

    private String afternoonLeaverToken;
    private String morningLeaverToken;
    private String morningCandidateToken;
    private StaffProfile morningLeaver;
    private StaffProfile afternoonLeaver;
    private StaffProfile morningCandidate;
    private StaffProfile afternoonCandidate;
    private Shift morningShift;
    private Shift afternoonShift;

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

        User morningLeaverUser = userRepository.save(User.builder()
                .username("staff-morning-leaver")
                .password(passwordEncoder.encode("password123"))
                .role(Role.STAFF)
                .build());
        User afternoonLeaverUser = userRepository.save(User.builder()
                .username("staff-aft")
                .password(passwordEncoder.encode("password123"))
                .role(Role.STAFF)
                .build());
        User morningCandidateUser = userRepository.save(User.builder()
                .username("staff-morn")
                .password(passwordEncoder.encode("password123"))
                .role(Role.STAFF)
                .build());
        User afternoonCandidateUser = userRepository.save(User.builder()
                .username("staff-afternoon")
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
        afternoonShift = shiftRepository.save(Shift.builder()
                .shiftCode("AFT")
                .shiftName("Afternoon Shift")
                .startTime(LocalTime.of(13, 0))
                .endTime(LocalTime.of(17, 0))
                .durationMinutes(240)
                .build());

        morningLeaver = staffProfileRepository.save(StaffProfile.builder()
                .user(morningLeaverUser)
                .fullName("Morning Leaver")
                .email("morning-leaver@test.local")
                .phone("0000000000")
                .preferredShift(morningShift)
                .availabilityStatus(AvailabilityStatus.OFF_DUTY)
                .build());
        afternoonLeaver = staffProfileRepository.save(StaffProfile.builder()
                .user(afternoonLeaverUser)
                .fullName("Afternoon Leaver")
                .email("aft@test.local")
                .phone("1111111111")
                .preferredShift(afternoonShift)
                .availabilityStatus(AvailabilityStatus.OFF_DUTY)
                .build());
        morningCandidate = staffProfileRepository.save(StaffProfile.builder()
                .user(morningCandidateUser)
                .fullName("Morning Candidate")
                .email("morning@test.local")
                .phone("2222222222")
                .preferredShift(morningShift)
                .availabilityStatus(AvailabilityStatus.OFF_DUTY)
                .build());
        afternoonCandidate = staffProfileRepository.save(StaffProfile.builder()
                .user(afternoonCandidateUser)
                .fullName("Afternoon Candidate")
                .email("afternoon@test.local")
                .phone("3333333333")
                .preferredShift(afternoonShift)
                .availabilityStatus(AvailabilityStatus.OFF_DUTY)
                .build());

        morningLeaverToken = authenticate("staff-morning-leaver", "password123");
        afternoonLeaverToken = authenticate("staff-aft", "password123");
        morningCandidateToken = authenticate("staff-morn", "password123");
    }

    @Test
    void morningTaskCanBeReassignedToAfternoonStaff() throws Exception {
        mockMvc.perform(post("/api/attendance/clock-in")
                        .header("Authorization", "Bearer " + morningCandidateToken))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/attendance/clock-out")
                        .header("Authorization", "Bearer " + morningCandidateToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/attendance/clock-in")
                        .header("Authorization", "Bearer " + morningLeaverToken))
                .andExpect(status().isOk());

        assignTask(morningLeaver, 500, morningShift, TaskType.DAILY_CLEAN, TaskStatus.ASSIGNED);

        mockMvc.perform(post("/api/attendance/clock-out")
                        .header("Authorization", "Bearer " + morningLeaverToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        assertThat(taskAssignmentRepository.findAllByTaskDate(TODAY))
                .hasSize(1)
                .allSatisfy(assignment -> {
                    assertThat(assignment.getStaff().getId()).isEqualTo(afternoonCandidate.getId());
                    assertThat(assignment.getCleaningTask().getShift().getId()).isEqualTo(afternoonShift.getId());
                });
    }

    @Test
    void afternoonTaskIsReassignedOnlyToAfternoonStaff() throws Exception {
        mockMvc.perform(post("/api/attendance/clock-in")
                        .header("Authorization", "Bearer " + afternoonLeaverToken))
                .andExpect(status().isOk());

        assignTask(afternoonLeaver, 501, afternoonShift, TaskType.DAILY_CLEAN, TaskStatus.ASSIGNED);

        mockMvc.perform(post("/api/attendance/clock-out")
                        .header("Authorization", "Bearer " + afternoonLeaverToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        assertThat(taskAssignmentRepository.findAllByTaskDate(TODAY))
                .hasSize(1)
                .allSatisfy(assignment -> {
                    assertThat(assignment.getStaff().getId()).isEqualTo(afternoonCandidate.getId());
                    assertThat(assignment.getCleaningTask().getShift().getId()).isEqualTo(afternoonShift.getId());
                })
                .noneSatisfy(assignment -> assertThat(assignment.getStaff().getId()).isEqualTo(morningCandidate.getId()));
    }

    private void assignTask(
            StaffProfile assignee,
            int roomNumber,
            Shift shift,
            TaskType taskType,
            TaskStatus taskStatus
    ) {
        Room room = roomRepository.save(Room.builder()
                .roomNumber(roomNumber)
                .roomStatus(RoomStatus.ACTIVE)
                .build());

        CleaningTask task = cleaningTaskRepository.save(CleaningTask.builder()
                .room(room)
                .taskDate(TODAY)
                .shift(shift)
                .taskType(taskType)
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
