package com.ibe.housekeeping.admin.attendance;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibe.housekeeping.attendance.repository.AttendanceRepository;
import com.ibe.housekeeping.auth.dto.LoginRequest;
import com.ibe.housekeeping.auth.repository.UserRepository;
import com.ibe.housekeeping.common.enums.AvailabilityStatus;
import com.ibe.housekeeping.common.enums.Role;
import com.ibe.housekeeping.entity.Attendance;
import com.ibe.housekeeping.entity.Shift;
import com.ibe.housekeeping.entity.StaffProfile;
import com.ibe.housekeeping.entity.User;
import com.ibe.housekeeping.shift.repository.ShiftRepository;
import com.ibe.housekeeping.staff.repository.StaffProfileRepository;
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
class AdminAttendanceControllerIntegrationTest {

    private static final LocalDate RANGE_START = LocalDate.of(2026, 3, 20);
    private static final LocalDate RANGE_END = LocalDate.of(2026, 3, 27);

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private StaffProfileRepository staffProfileRepository;

    @Autowired
    private ShiftRepository shiftRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String adminToken;
    private String staffToken;
    private Shift morningShift;
    private Shift eveningShift;

    @BeforeEach
    void setUp() throws Exception {
        attendanceRepository.deleteAll();
        staffProfileRepository.deleteAll();
        shiftRepository.deleteAll();
        userRepository.deleteAll();

        User admin = userRepository.save(User.builder()
                .username("attendance-admin")
                .password(passwordEncoder.encode("password123"))
                .role(Role.ADMIN)
                .build());

        User staffUser = userRepository.save(User.builder()
                .username("attendance-staff")
                .password(passwordEncoder.encode("password123"))
                .role(Role.STAFF)
                .build());

        morningShift = shiftRepository.save(Shift.builder()
                .shiftCode("MORN-A")
                .shiftName("Morning Shift")
                .startTime(LocalTime.of(8, 0))
                .endTime(LocalTime.of(16, 0))
                .durationMinutes(480)
                .build());

        eveningShift = shiftRepository.save(Shift.builder()
                .shiftCode("EVE-B")
                .shiftName("Evening Shift")
                .startTime(LocalTime.of(14, 0))
                .endTime(LocalTime.of(22, 0))
                .durationMinutes(480)
                .build());

        StaffProfile elena = createStaff("elena.staff", "Elena Rodriguez", morningShift);
        StaffProfile marcus = createStaff("marcus.staff", "Marcus Chen", eveningShift);
        createStaff("attendance-staff-profile", "Staff Viewer", morningShift);

        seedAttendance(elena, marcus);

        adminToken = authenticate(admin.getUsername(), "password123");
        staffToken = authenticate(staffUser.getUsername(), "password123");
    }

    @Test
    void attendanceSummaryReturnsAdminMetricsForSelectedDateRange() throws Exception {
        mockMvc.perform(get("/api/admin/attendance/summary")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("fromDate", RANGE_START.toString())
                        .param("toDate", RANGE_END.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fromDate").value("2026-03-20"))
                .andExpect(jsonPath("$.toDate").value("2026-03-27"))
                .andExpect(jsonPath("$.activeTodayCount").value(2))
                .andExpect(jsonPath("$.activeTodayDeltaPercent").isNumber())
                .andExpect(jsonPath("$.averageShiftLengthHours").value(8.3))
                .andExpect(jsonPath("$.lateCheckInsCount").value(1))
                .andExpect(jsonPath("$.averageShiftLengthLabel").value("Across completed shifts"));
    }

    @Test
    void attendanceLogsSupportsSearchFlagsAndPagination() throws Exception {
        mockMvc.perform(get("/api/admin/attendance/logs")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("fromDate", RANGE_START.toString())
                        .param("toDate", RANGE_END.toString())
                        .param("search", "marcus")
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(5))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].staffName").value("Marcus Chen"))
                .andExpect(jsonPath("$.items[0].staffRoleLabel").value("Housekeeping Staff"))
                .andExpect(jsonPath("$.items[0].staffInitials").value("MC"))
                .andExpect(jsonPath("$.items[0].clockInTime").value("14:20"))
                .andExpect(jsonPath("$.items[0].clockOutTime").value("22:50"))
                .andExpect(jsonPath("$.items[0].totalWorkedHours").value(8.5))
                .andExpect(jsonPath("$.items[0].lateCheckIn").value(true))
                .andExpect(jsonPath("$.items[0].overtimeFlag").value(true))
                .andExpect(jsonPath("$.items[0].statusTag").value("Overtime"));
    }

    @Test
    void attendanceEndpointsValidateDateRangeAndProtectAccess() throws Exception {
        mockMvc.perform(get("/api/admin/attendance/logs")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("fromDate", RANGE_END.toString())
                        .param("toDate", RANGE_START.toString()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.message").value("toDate must be on or after fromDate."));

        mockMvc.perform(get("/api/admin/attendance/summary")
                        .header("Authorization", "Bearer " + staffToken)
                        .param("fromDate", RANGE_START.toString())
                        .param("toDate", RANGE_END.toString()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.message").value("Access denied"));

        mockMvc.perform(get("/api/admin/attendance/summary")
                        .param("fromDate", RANGE_START.toString())
                        .param("toDate", RANGE_END.toString()))
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
                .availabilityStatus(AvailabilityStatus.OFF_DUTY)
                .build());
    }

    private void seedAttendance(StaffProfile elena, StaffProfile marcus) {
        attendanceRepository.save(Attendance.builder()
                .staff(elena)
                .shift(morningShift)
                .workDate(LocalDate.of(2026, 3, 27))
                .clockInTime(LocalDateTime.of(2026, 3, 27, 8, 0))
                .clockOutTime(LocalDateTime.of(2026, 3, 27, 16, 0))
                .workedMinutes(480)
                .build());

        attendanceRepository.save(Attendance.builder()
                .staff(marcus)
                .shift(eveningShift)
                .workDate(LocalDate.of(2026, 3, 27))
                .clockInTime(LocalDateTime.of(2026, 3, 27, 14, 20))
                .clockOutTime(LocalDateTime.of(2026, 3, 27, 22, 50))
                .workedMinutes(510)
                .build());

        attendanceRepository.save(Attendance.builder()
                .staff(elena)
                .shift(morningShift)
                .workDate(LocalDate.of(2026, 3, 19))
                .clockInTime(LocalDateTime.of(2026, 3, 19, 8, 0))
                .clockOutTime(LocalDateTime.of(2026, 3, 19, 16, 0))
                .workedMinutes(480)
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

