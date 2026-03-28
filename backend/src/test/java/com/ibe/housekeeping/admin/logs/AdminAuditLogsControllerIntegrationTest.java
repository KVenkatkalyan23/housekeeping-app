package com.ibe.housekeeping.admin.logs;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibe.housekeeping.activitylog.entity.ActivityLog;
import com.ibe.housekeeping.activitylog.repository.ActivityLogRepository;
import com.ibe.housekeeping.auth.dto.LoginRequest;
import com.ibe.housekeeping.auth.repository.UserRepository;
import com.ibe.housekeeping.common.enums.ActorType;
import com.ibe.housekeeping.common.enums.EventCategory;
import com.ibe.housekeeping.common.enums.Role;
import com.ibe.housekeeping.common.enums.Severity;
import com.ibe.housekeeping.entity.User;
import java.util.UUID;
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
class AdminAuditLogsControllerIntegrationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ActivityLogRepository activityLogRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String adminToken;
    private String staffToken;

    @BeforeEach
    void setUp() throws Exception {
        activityLogRepository.deleteAll();
        userRepository.deleteAll();

        User admin = userRepository.save(User.builder()
                .username("audit-admin")
                .password(passwordEncoder.encode("password123"))
                .role(Role.ADMIN)
                .build());

        User staff = userRepository.save(User.builder()
                .username("audit-staff")
                .password(passwordEncoder.encode("password123"))
                .role(Role.STAFF)
                .build());

        activityLogRepository.save(ActivityLog.builder()
                .eventCode("ALLOCATION_RUN")
                .eventTitle("Automated Resource Check")
                .eventMessage("Allocation run completed.")
                .eventCategory(EventCategory.SYSTEM)
                .severity(Severity.SUCCESS)
                .actorType(ActorType.SYSTEM)
                .actorName("System")
                .targetEntityType("ALLOCATION")
                .targetEntityId(UUID.randomUUID())
                .metadata("{\"actorSubtitle\":\"Automated Service\",\"targetLabel\":\"SERVER\",\"targetSubLabel\":\"ID: SYS-01\"}")
                .build());

        activityLogRepository.save(ActivityLog.builder()
                .eventCode("TASK_RELOCATED")
                .eventTitle("Room Assignment Change")
                .eventMessage("Task moved to a new staff member.")
                .eventCategory(EventCategory.TASK)
                .severity(Severity.WARNING)
                .actorType(ActorType.USER)
                .actorUserId(admin.getId())
                .actorName("Marcus V.")
                .targetEntityType("TASK")
                .targetEntityId(UUID.randomUUID())
                .metadata("{\"actorSubtitle\":\"Floor Manager\",\"targetLabel\":\"ROOM\",\"targetSubLabel\":\"ID: RM-402\"}")
                .build());

        adminToken = authenticate("audit-admin", "password123");
        staffToken = authenticate("audit-staff", "password123");
    }

    @Test
    void adminLogsReturnsPaginatedItemsAndSupportsCategoryFilter() throws Exception {
        mockMvc.perform(get("/api/admin/logs")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(2))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.items[0].eventCode").exists())
                .andExpect(jsonPath("$.items[0].actorName").isString());

        mockMvc.perform(get("/api/admin/logs")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("category", "SYSTEM"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].eventCode").value("ALLOCATION_RUN"))
                .andExpect(jsonPath("$.items[0].eventCategory").value("SYSTEM"));
    }

    @Test
    void adminLogsIsForbiddenForStaff() throws Exception {
        mockMvc.perform(get("/api/admin/logs")
                        .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.message").value("Access denied"));
    }

    @Test
    void adminLogsRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/admin/logs"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.message").value("Authentication required"));
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
