package com.ibe.housekeeping.admin.staff;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibe.housekeeping.auth.dto.LoginRequest;
import com.ibe.housekeeping.auth.repository.UserRepository;
import com.ibe.housekeeping.common.enums.AvailabilityStatus;
import com.ibe.housekeeping.common.enums.LeaveStatus;
import com.ibe.housekeeping.common.enums.LeaveType;
import com.ibe.housekeeping.common.enums.Role;
import com.ibe.housekeeping.entity.LeaveRequest;
import com.ibe.housekeeping.entity.Shift;
import com.ibe.housekeeping.entity.StaffProfile;
import com.ibe.housekeeping.entity.User;
import com.ibe.housekeeping.leave.repository.LeaveRequestRepository;
import com.ibe.housekeeping.shift.repository.ShiftRepository;
import com.ibe.housekeeping.staff.repository.StaffProfileRepository;
import com.ibe.housekeeping.support.FixedClockTestConfig;
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
class AdminStaffDirectoryControllerIntegrationTest {

    private static final LocalDate FIXED_TODAY = LocalDate.of(2026, 3, 27);

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
    private ShiftRepository shiftRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String adminToken;
    private String staffToken;
    private StaffProfile onDutyStaff;
    private StaffProfile offDutyStaff;
    private StaffProfile leaveStaff;
    private StaffProfile sickStaff;
    private StaffProfile sickPrecedenceStaff;
    private StaffProfile nullContactStaff;

    @BeforeEach
    void setUp() throws Exception {
        leaveRequestRepository.deleteAll();
        staffProfileRepository.deleteAll();
        shiftRepository.deleteAll();
        userRepository.deleteAll();

        Shift morningShift = shiftRepository.save(Shift.builder()
                .shiftCode("MORN")
                .shiftName("Morning Shift")
                .startTime(LocalTime.of(8, 0))
                .endTime(LocalTime.of(12, 0))
                .durationMinutes(240)
                .build());

        User admin = userRepository.save(User.builder()
                .username("admin-user")
                .password(passwordEncoder.encode("password123"))
                .role(Role.ADMIN)
                .build());

        User staffUser = userRepository.save(User.builder()
                .username("staff-user")
                .password(passwordEncoder.encode("password123"))
                .role(Role.STAFF)
                .build());

        onDutyStaff = createStaff(
                "andrea-on-duty",
                "Andrea On Duty",
                "andrea@housekeeping.local",
                "1111111111",
                AvailabilityStatus.ON_DUTY,
                morningShift
        );
        offDutyStaff = createStaff(
                "brian-off-duty",
                "Brian Off Duty",
                "brian@housekeeping.local",
                "2222222222",
                AvailabilityStatus.OFF_DUTY,
                morningShift
        );
        leaveStaff = createStaff(
                "charlie-leave",
                "Charlie Leave",
                "charlie@housekeeping.local",
                "3333333333",
                AvailabilityStatus.ON_DUTY,
                morningShift
        );
        sickStaff = createStaff(
                "daisy-sick",
                "Daisy Sick",
                "daisy@housekeeping.local",
                "4444444444",
                AvailabilityStatus.OFF_DUTY,
                morningShift
        );
        sickPrecedenceStaff = createStaff(
                "evan-both-leaves",
                "Evan Both",
                "evan@housekeeping.local",
                "5555555555",
                AvailabilityStatus.ON_DUTY,
                morningShift
        );
        nullContactStaff = createStaff(
                "frank-null-contact",
                "Frank Null",
                null,
                null,
                AvailabilityStatus.OFF_DUTY,
                morningShift
        );

        createActiveLeave(leaveStaff, LeaveType.PLANNED);
        createActiveLeave(sickStaff, LeaveType.SICK);
        createActiveLeave(sickPrecedenceStaff, LeaveType.PLANNED);
        createActiveLeave(sickPrecedenceStaff, LeaveType.SICK);
        leaveRequestRepository.save(LeaveRequest.builder()
                .staff(offDutyStaff)
                .leaveStartDate(FIXED_TODAY.plusDays(1))
                .leaveEndDate(FIXED_TODAY.plusDays(2))
                .leaveType(LeaveType.SICK)
                .status(LeaveStatus.APPROVED)
                .build());

        adminToken = authenticate(admin.getUsername(), "password123");
        staffToken = authenticate(staffUser.getUsername(), "password123");
    }

    @Test
    void adminStaffDirectoryReturnsResolvedStatusesSummaryAndPagination() throws Exception {
        mockMvc.perform(get("/api/admin/staff")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("page", "0")
                        .param("size", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(3))
                .andExpect(jsonPath("$.totalElements").value(6))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.summary.totalEmployees").value(6))
                .andExpect(jsonPath("$.summary.onDutyCount").value(1))
                .andExpect(jsonPath("$.summary.offDutyCount").value(2))
                .andExpect(jsonPath("$.summary.leaveCount").value(1))
                .andExpect(jsonPath("$.summary.sickCount").value(2))
                .andExpect(jsonPath("$.items.length()").value(3))
                .andExpect(jsonPath("$.items[0].fullName").value("Andrea On Duty"))
                .andExpect(jsonPath("$.items[0].status").value("ON_DUTY"))
                .andExpect(jsonPath("$.items[1].fullName").value("Brian Off Duty"))
                .andExpect(jsonPath("$.items[1].status").value("OFF_DUTY"))
                .andExpect(jsonPath("$.items[2].fullName").value("Charlie Leave"))
                .andExpect(jsonPath("$.items[2].status").value("LEAVE"));
    }

    @Test
    void adminStaffDirectoryAppliesStatusFiltersUsingResolvedStatus() throws Exception {
        mockMvc.perform(get("/api/admin/staff")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("status", "ON_DUTY"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].fullName").value("Andrea On Duty"))
                .andExpect(jsonPath("$.items[0].status").value("ON_DUTY"));

        mockMvc.perform(get("/api/admin/staff")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("status", "OFF_DUTY"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.items.length()").value(2))
                .andExpect(jsonPath("$.items[0].status").value("OFF_DUTY"))
                .andExpect(jsonPath("$.items[1].status").value("OFF_DUTY"));

        mockMvc.perform(get("/api/admin/staff")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("status", "LEAVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.items[0].fullName").value("Charlie Leave"))
                .andExpect(jsonPath("$.items[0].status").value("LEAVE"));

        mockMvc.perform(get("/api/admin/staff")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("status", "SICK"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.items.length()").value(2))
                .andExpect(jsonPath("$.items[0].status").value("SICK"))
                .andExpect(jsonPath("$.items[1].status").value("SICK"));
    }

    @Test
    void adminStaffDirectorySupportsCaseInsensitiveSearchAndKeepsGlobalSummary() throws Exception {
        mockMvc.perform(get("/api/admin/staff")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("search", "dAiSy"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].fullName").value("Daisy Sick"))
                .andExpect(jsonPath("$.items[0].status").value("SICK"))
                .andExpect(jsonPath("$.summary.totalEmployees").value(6))
                .andExpect(jsonPath("$.summary.sickCount").value(2));

        mockMvc.perform(get("/api/admin/staff")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("search", "frank-null-contact"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.items[0].fullName").value("Frank Null"));
    }

    @Test
    void adminStaffDirectoryReturnsSafeNullContactFieldsAndDerivedStaffCode() throws Exception {
        mockMvc.perform(get("/api/admin/staff")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("search", "Frank Null"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].email").isEmpty())
                .andExpect(jsonPath("$.items[0].phone").isEmpty())
                .andExpect(jsonPath("$.items[0].staffCode").value(org.hamcrest.Matchers.matchesPattern("STF-[A-F0-9]{4}")))
                .andExpect(jsonPath("$.items[0].userId").isNotEmpty());
    }

    @Test
    void adminStaffDirectoryReturnsEmptyPageForOutOfRangePagination() throws Exception {
        mockMvc.perform(get("/api/admin/staff")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("page", "3")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(3))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.totalElements").value(6))
                .andExpect(jsonPath("$.totalPages").value(3))
                .andExpect(jsonPath("$.items.length()").value(0));
    }

    @Test
    void adminStaffDirectoryRejectsInvalidStatusAndPaginationParameters() throws Exception {
        mockMvc.perform(get("/api/admin/staff")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("status", "UNKNOWN"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.message").value("status must be one of ALL, ON_DUTY, OFF_DUTY, LEAVE, or SICK."));

        mockMvc.perform(get("/api/admin/staff")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("page", "-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.message").value("page must be 0 or greater."));

        mockMvc.perform(get("/api/admin/staff")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("size", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.message").value("size must be at least 1."));
    }

    @Test
    void adminStaffDirectoryIsForbiddenForStaff() throws Exception {
        mockMvc.perform(get("/api/admin/staff")
                        .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.message").value("Access denied"));
    }

    @Test
    void adminStaffDirectoryRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/admin/staff"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.message").value("Authentication required"));
    }

    private StaffProfile createStaff(
            String username,
            String fullName,
            String email,
            String phone,
            AvailabilityStatus availabilityStatus,
            Shift shift
    ) {
        User user = userRepository.save(User.builder()
                .username(username)
                .password(passwordEncoder.encode("password123"))
                .role(Role.STAFF)
                .build());

        return staffProfileRepository.save(StaffProfile.builder()
                .user(user)
                .fullName(fullName)
                .email(email)
                .phone(phone)
                .preferredShift(shift)
                .availabilityStatus(availabilityStatus)
                .build());
    }

    private void createActiveLeave(StaffProfile staffProfile, LeaveType leaveType) {
        leaveRequestRepository.save(LeaveRequest.builder()
                .staff(staffProfile)
                .leaveStartDate(FIXED_TODAY.minusDays(1))
                .leaveEndDate(FIXED_TODAY.plusDays(1))
                .leaveType(leaveType)
                .status(LeaveStatus.APPROVED)
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
