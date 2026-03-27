package com.ibe.housekeeping.leave;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibe.housekeeping.auth.dto.LoginRequest;
import com.ibe.housekeeping.auth.repository.UserRepository;
import com.ibe.housekeeping.common.enums.AvailabilityStatus;
import com.ibe.housekeeping.common.enums.Role;
import com.ibe.housekeeping.entity.StaffProfile;
import com.ibe.housekeeping.entity.User;
import com.ibe.housekeeping.leave.repository.LeaveRequestRepository;
import com.ibe.housekeeping.shift.repository.ShiftRepository;
import com.ibe.housekeeping.staff.repository.StaffProfileRepository;
import java.time.LocalDate;
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
    private ShiftRepository shiftRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User staffUser;
    private User otherStaffUser;
    private String staffToken;
    private String adminToken;

    @BeforeEach
    void setUp() throws Exception {
        leaveRequestRepository.deleteAll();
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

        createStaffProfile(staffUser, "Staff One");
        createStaffProfile(otherStaffUser, "Staff Two");

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

    private void createStaffProfile(User user, String fullName) {
        staffProfileRepository.save(StaffProfile.builder()
                .user(user)
                .fullName(fullName)
                .email(user.getUsername() + "@housekeeping.local")
                .phone("9999999999")
                .availabilityStatus(AvailabilityStatus.OFF_DUTY)
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
