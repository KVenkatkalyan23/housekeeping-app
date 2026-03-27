package com.ibe.housekeeping.leave;

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
import com.ibe.housekeeping.leave.repository.LeaveRequestRepository;
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
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import com.ibe.housekeeping.support.FixedClockTestConfig;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(FixedClockTestConfig.class)
class LeaveControllerIntegrationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StaffProfileRepository staffProfileRepository;

    @Autowired
    private LeaveRequestRepository leaveRequestRepository;

    @Autowired
    private TaskAssignmentRepository taskAssignmentRepository;

    @Autowired
    private CleaningTaskRepository cleaningTaskRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private RoomStayRepository roomStayRepository;

    @Autowired
    private ShiftRepository shiftRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User staffUser;
    private User otherStaffUser;
    private String staffToken;
    private String adminToken;
    private StaffProfile staffProfile;
    private StaffProfile otherStaffProfile;
    private Shift morningShift;

    @BeforeEach
    void setUp() throws Exception {
        taskAssignmentRepository.deleteAll();
        cleaningTaskRepository.deleteAll();
        leaveRequestRepository.deleteAll();
        roomStayRepository.deleteAll();
        roomRepository.deleteAll();
        staffProfileRepository.deleteAll();
        shiftRepository.deleteAll();
        userRepository.deleteAll();

        User admin = userRepository.save(User.builder()
                .username("admin")
                .password(passwordEncoder.encode("password123"))
                .role(Role.ADMIN)
                .build());

        staffUser = userRepository.save(User.builder()
                .username("staff-1")
                .password(passwordEncoder.encode("password123"))
                .role(Role.STAFF)
                .build());

        otherStaffUser = userRepository.save(User.builder()
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

        staffProfile = createStaffProfile(staffUser, "Staff One");
        otherStaffProfile = createStaffProfile(otherStaffUser, "Staff Two");

        staffToken = authenticate("staff-1", "password123");
        adminToken = authenticate(admin.getUsername(), "password123");
    }

    @Test
    void applySickLeaveSucceedsAndAutoApproves() throws Exception {
        LocalDate tomorrow = LocalDate.now().plusDays(1);

        mockMvc.perform(post("/api/leave/apply")
                        .header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId":"%s",
                                  "fromDate":"%s",
                                  "toDate":"%s",
                                  "leaveType":"SICK",
                                  "reason":"Fever"
                                }
                                """.formatted(staffUser.getId(), tomorrow, tomorrow.plusDays(1))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(staffUser.getId().toString()))
                .andExpect(jsonPath("$.leaveType").value("SICK"))
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andExpect(jsonPath("$.durationDays").value(2));
    }

    @Test
    void applyPlannedLeaveSucceedsWhenSubmittedTwoDaysAhead() throws Exception {
        LocalDate fromDate = LocalDate.now().plusDays(2);

        mockMvc.perform(post("/api/leave/apply")
                        .header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId":"%s",
                                  "fromDate":"%s",
                                  "toDate":"%s",
                                  "leaveType":"PLANNED"
                                }
                                """.formatted(staffUser.getId(), fromDate, fromDate.plusDays(2))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.leaveType").value("PLANNED"))
                .andExpect(jsonPath("$.durationDays").value(3));
    }

    @Test
    void plannedLeaveIsRejectedWhenLessThanTwoDaysAhead() throws Exception {
        LocalDate tomorrow = LocalDate.now().plusDays(1);

        mockMvc.perform(post("/api/leave/apply")
                        .header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId":"%s",
                                  "fromDate":"%s",
                                  "toDate":"%s",
                                  "leaveType":"PLANNED"
                                }
                                """.formatted(staffUser.getId(), tomorrow, tomorrow)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.message").value("PLANNED leave must be applied at least 2 days before fromDate."));
    }

    @Test
    void leaveIsRejectedWhenDurationExceedsSevenDays() throws Exception {
        LocalDate fromDate = LocalDate.now().plusDays(2);

        mockMvc.perform(post("/api/leave/apply")
                        .header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId":"%s",
                                  "fromDate":"%s",
                                  "toDate":"%s",
                                  "leaveType":"PLANNED"
                                }
                                """.formatted(staffUser.getId(), fromDate, fromDate.plusDays(7))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.message").value("PLANNED leave cannot exceed 7 days."));
    }

    @Test
    void leaveIsRejectedWhenDatesAreInvalid() throws Exception {
        LocalDate fromDate = LocalDate.now().plusDays(3);

        mockMvc.perform(post("/api/leave/apply")
                        .header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId":"%s",
                                  "fromDate":"%s",
                                  "toDate":"%s",
                                  "leaveType":"SICK"
                                }
                                """.formatted(staffUser.getId(), fromDate, fromDate.minusDays(1))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.message").value("toDate must be on or after fromDate."));
    }

    @Test
    void myLeavesReturnsPaginatedUserSpecificData() throws Exception {
        LocalDate fromDate = LocalDate.now().plusDays(2);
        applyLeaveFor(staffToken, staffUser.getId().toString(), fromDate, fromDate, "SICK");
        applyLeaveFor(staffToken, staffUser.getId().toString(), fromDate.plusDays(1), fromDate.plusDays(1), "SICK");
        String otherStaffToken = authenticate("staff-2", "password123");
        applyLeaveFor(otherStaffToken, otherStaffUser.getId().toString(), fromDate.plusDays(2), fromDate.plusDays(2), "SICK");

        mockMvc.perform(get("/api/leave/my")
                        .header("Authorization", "Bearer " + staffToken)
                        .param("userId", staffUser.getId().toString())
                        .param("page", "0")
                        .param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].userId").value(staffUser.getId().toString()))
                .andExpect(jsonPath("$.pagination.page").value(0))
                .andExpect(jsonPath("$.pagination.size").value(1))
                .andExpect(jsonPath("$.pagination.hasNext").value(true));
    }

    @Test
    void adminLeavesReturnsPaginatedAllUserData() throws Exception {
        LocalDate fromDate = LocalDate.now().plusDays(2);
        applyLeaveFor(staffToken, staffUser.getId().toString(), fromDate, fromDate, "PLANNED");
        String otherStaffToken = authenticate("staff-2", "password123");
        applyLeaveFor(otherStaffToken, otherStaffUser.getId().toString(), fromDate.plusDays(3), fromDate.plusDays(3), "SICK");

        mockMvc.perform(get("/api/leave/admin")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("page", "0")
                        .param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].staffName").isNotEmpty())
                .andExpect(jsonPath("$.pagination.size").value(1))
                .andExpect(jsonPath("$.pagination.hasNext").value(true));
    }

    @Test
    void applyLeaveForTodayDetachesAndReassignsPendingTasks() throws Exception {
        LocalDate today = LocalDate.now();
        otherStaffProfile.setAvailabilityStatus(AvailabilityStatus.ON_DUTY);
        staffProfileRepository.save(otherStaffProfile);
        assignTask(staffProfile, 301, TaskType.DAILY_CLEAN, TaskStatus.ASSIGNED, today);

        mockMvc.perform(post("/api/leave/apply")
                        .header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId":"%s",
                                  "fromDate":"%s",
                                  "toDate":"%s",
                                  "leaveType":"SICK"
                                }
                                """.formatted(staffUser.getId(), today, today)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.leaveType").value("SICK"));

        mockMvc.perform(get("/api/leave/my")
                        .header("Authorization", "Bearer " + staffToken)
                        .param("userId", staffUser.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1));

        assertThat(taskAssignmentRepository.findAllByTaskDate(today))
                .hasSize(1)
                .allSatisfy(assignment -> assertThat(assignment.getStaff().getId())
                        .isEqualTo(otherStaffProfile.getId()));
    }

    @Test
    void applyLeaveForFutureDateDoesNotDetachTodaysAssignments() throws Exception {
        LocalDate today = LocalDate.now();
        LocalDate futureDate = today.plusDays(2);
        assignTask(staffProfile, 302, TaskType.DAILY_CLEAN, TaskStatus.ASSIGNED, today);

        mockMvc.perform(post("/api/leave/apply")
                        .header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId":"%s",
                                  "fromDate":"%s",
                                  "toDate":"%s",
                                  "leaveType":"PLANNED"
                                }
                                """.formatted(staffUser.getId(), futureDate, futureDate)))
                .andExpect(status().isCreated());

        assertThat(taskAssignmentRepository.findAllByTaskDate(today))
                .hasSize(1)
                .allSatisfy(assignment -> assertThat(assignment.getStaff().getId()).isEqualTo(staffProfile.getId()));
    }

    @Test
    void applyLeaveForTodayLeavesTasksUnassignedWhenNoOnDutyStaffCanTakeThem() throws Exception {
        LocalDate today = LocalDate.now();
        leaveRequestRepository.save(com.ibe.housekeeping.entity.LeaveRequest.builder()
                .staff(otherStaffProfile)
                .leaveStartDate(today)
                .leaveEndDate(today)
                .leaveType(com.ibe.housekeeping.common.enums.LeaveType.SICK)
                .status(com.ibe.housekeeping.common.enums.LeaveStatus.APPROVED)
                .build());
        assignTask(staffProfile, 303, TaskType.DAILY_CLEAN, TaskStatus.ASSIGNED, today);

        mockMvc.perform(post("/api/leave/apply")
                        .header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId":"%s",
                                  "fromDate":"%s",
                                  "toDate":"%s",
                                  "leaveType":"SICK"
                                }
                                """.formatted(staffUser.getId(), today, today)))
                .andExpect(status().isCreated());

        assertThat(taskAssignmentRepository.findAllByTaskDate(today)).isEmpty();
        assertThat(cleaningTaskRepository.findAllByTaskDateOrderByPriorityOrderAscRoomRoomNumberAsc(today))
                .hasSize(1)
                .allSatisfy(task -> assertThat(task.getTaskStatus()).isEqualTo(TaskStatus.PENDING));
    }

    @Test
    void applyLeaveCanReassignToOffDutyStaffWhoDidNotClockOutToday() throws Exception {
        LocalDate today = LocalDate.now();
        assignTask(staffProfile, 310, TaskType.DAILY_CLEAN, TaskStatus.ASSIGNED, today);

        mockMvc.perform(post("/api/leave/apply")
                        .header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId":"%s",
                                  "fromDate":"%s",
                                  "toDate":"%s",
                                  "leaveType":"SICK"
                                }
                                """.formatted(staffUser.getId(), today, today)))
                .andExpect(status().isCreated());

        assertThat(taskAssignmentRepository.findAllByTaskDate(today))
                .hasSize(1)
                .allSatisfy(assignment -> assertThat(assignment.getStaff().getId()).isEqualTo(otherStaffProfile.getId()));
    }

    @Test
    void applyLeaveForTodayDetachesPendingAndAssignedButKeepsCompletedAndInProgressTasks() throws Exception {
        LocalDate today = LocalDate.now();
        otherStaffProfile.setAvailabilityStatus(AvailabilityStatus.ON_DUTY);
        staffProfileRepository.save(otherStaffProfile);
        assignTask(staffProfile, 304, TaskType.DAILY_CLEAN, TaskStatus.PENDING, today);
        assignTask(staffProfile, 305, TaskType.DAILY_CLEAN, TaskStatus.ASSIGNED, today);
        assignTask(staffProfile, 306, TaskType.DAILY_CLEAN, TaskStatus.IN_PROGRESS, today);
        assignTask(staffProfile, 307, TaskType.DAILY_CLEAN, TaskStatus.COMPLETED, today);

        mockMvc.perform(post("/api/leave/apply")
                        .header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId":"%s",
                                  "fromDate":"%s",
                                  "toDate":"%s",
                                  "leaveType":"SICK"
                                }
                                """.formatted(staffUser.getId(), today, today)))
                .andExpect(status().isCreated());

        assertThat(taskAssignmentRepository.findAllByTaskDate(today))
                .hasSize(4)
                .anySatisfy(assignment -> {
                    assertThat(assignment.getCleaningTask().getRoom().getRoomNumber()).isEqualTo(304);
                    assertThat(assignment.getStaff().getId()).isEqualTo(otherStaffProfile.getId());
                })
                .anySatisfy(assignment -> {
                    assertThat(assignment.getCleaningTask().getRoom().getRoomNumber()).isEqualTo(305);
                    assertThat(assignment.getStaff().getId()).isEqualTo(otherStaffProfile.getId());
                })
                .anySatisfy(assignment -> {
                    assertThat(assignment.getCleaningTask().getRoom().getRoomNumber()).isEqualTo(306);
                    assertThat(assignment.getStaff().getId()).isEqualTo(staffProfile.getId());
                })
                .anySatisfy(assignment -> {
                    assertThat(assignment.getCleaningTask().getRoom().getRoomNumber()).isEqualTo(307);
                    assertThat(assignment.getStaff().getId()).isEqualTo(staffProfile.getId());
                });
    }

    @Test
    void applyLeaveKeepsOtherStaffAssignmentsAndAddsRelocatedTasksOnTop() throws Exception {
        LocalDate today = LocalDate.now();
        otherStaffProfile.setAvailabilityStatus(AvailabilityStatus.ON_DUTY);
        staffProfileRepository.save(otherStaffProfile);
        assignTask(staffProfile, 308, TaskType.DAILY_CLEAN, TaskStatus.ASSIGNED, today);
        assignTask(otherStaffProfile, 309, TaskType.DAILY_CLEAN, TaskStatus.ASSIGNED, today);

        mockMvc.perform(post("/api/leave/apply")
                        .header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId":"%s",
                                  "fromDate":"%s",
                                  "toDate":"%s",
                                  "leaveType":"SICK"
                                }
                                """.formatted(staffUser.getId(), today, today)))
                .andExpect(status().isCreated());

        assertThat(taskAssignmentRepository.findAllByTaskDate(today))
                .hasSize(2)
                .allSatisfy(assignment -> assertThat(assignment.getStaff().getId()).isEqualTo(otherStaffProfile.getId()));
    }

    private void applyLeaveFor(
            String token,
            String userId,
            LocalDate fromDate,
            LocalDate toDate,
            String leaveType
    ) throws Exception {
        mockMvc.perform(post("/api/leave/apply")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId":"%s",
                                  "fromDate":"%s",
                                  "toDate":"%s",
                                  "leaveType":"%s"
                                }
                                """.formatted(userId, fromDate, toDate, leaveType)))
                .andExpect(status().isCreated());
    }

    private StaffProfile createStaffProfile(User user, String fullName) {
        return staffProfileRepository.save(StaffProfile.builder()
                .user(user)
                .fullName(fullName)
                .email(user.getUsername() + "@housekeeping.local")
                .phone("9999999999")
                .preferredShift(morningShift)
                .availabilityStatus(AvailabilityStatus.OFF_DUTY)
                .build());
    }

    private void assignTask(
            StaffProfile assignee,
            int roomNumber,
            TaskType taskType,
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
