package com.ibe.housekeeping.task;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibe.housekeeping.auth.dto.LoginRequest;
import com.ibe.housekeeping.auth.repository.UserRepository;
import com.ibe.housekeeping.allocation.repository.TaskAssignmentRepository;
import com.ibe.housekeeping.common.enums.Role;
import com.ibe.housekeeping.common.enums.RoomStatus;
import com.ibe.housekeeping.entity.Room;
import com.ibe.housekeeping.entity.RoomStay;
import com.ibe.housekeeping.entity.User;
import com.ibe.housekeeping.room.repository.RoomRepository;
import com.ibe.housekeeping.roomstay.repository.RoomStayRepository;
import com.ibe.housekeeping.task.repository.CleaningTaskRepository;
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
class CleaningTaskControllerIntegrationTest {

    private static final LocalDate TASK_DATE = LocalDate.of(2026, 3, 26);

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private RoomStayRepository roomStayRepository;

    @Autowired
    private CleaningTaskRepository cleaningTaskRepository;

    @Autowired
    private TaskAssignmentRepository taskAssignmentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String accessToken;

    @BeforeEach
    void setUp() throws Exception {
        taskAssignmentRepository.deleteAll();
        cleaningTaskRepository.deleteAll();
        roomStayRepository.deleteAll();
        roomRepository.deleteAll();
        userRepository.deleteAll();

        userRepository.save(User.builder()
                .username("admin")
                .password(passwordEncoder.encode("password123"))
                .role(Role.ADMIN)
                .build());

        accessToken = authenticate("admin", "password123");
    }

    @Test
    void generateTasksCreatesExpectedTaskTypesAndPreventsDuplicates() throws Exception {
        Room dailyRoom = createRoom(101);
        Room checkoutRoom = createRoom(102);
        Room vacantRoom = createRoom(103);

        createStay(dailyRoom, TASK_DATE.minusDays(1), TASK_DATE.plusDays(2));
        RoomStay checkoutStay = createStay(checkoutRoom, TASK_DATE.minusDays(2), TASK_DATE);
        createStay(vacantRoom, TASK_DATE.minusDays(5), TASK_DATE.minusDays(1));

        mockMvc.perform(post("/api/tasks/generate")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"taskDate\":\"" + TASK_DATE + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.generatedCount").value(3))
                .andExpect(jsonPath("$.skippedCount").value(0));

        mockMvc.perform(post("/api/tasks/generate")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"taskDate\":\"" + TASK_DATE + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.generatedCount").value(0))
                .andExpect(jsonPath("$.skippedCount").value(3));

        mockMvc.perform(get("/api/tasks")
                        .header("Authorization", "Bearer " + accessToken)
                        .param("taskDate", TASK_DATE.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].roomNumber").value(102))
                .andExpect(jsonPath("$[0].taskType").value("DEEP_CLEAN"))
                .andExpect(jsonPath("$[0].estimatedMinutes").value(120))
                .andExpect(jsonPath("$[0].priorityOrder").value(1))
                .andExpect(jsonPath("$[0].shiftId").isEmpty())
                .andExpect(jsonPath("$[0].sourceStayId").value(checkoutStay.getId().toString()))
                .andExpect(jsonPath("$[1].roomNumber").value(101))
                .andExpect(jsonPath("$[1].taskType").value("DAILY_CLEAN"))
                .andExpect(jsonPath("$[1].estimatedMinutes").value(30))
                .andExpect(jsonPath("$[1].priorityOrder").value(2))
                .andExpect(jsonPath("$[2].roomNumber").value(103))
                .andExpect(jsonPath("$[2].taskType").value("VACANT_CLEAN"))
                .andExpect(jsonPath("$[2].estimatedMinutes").value(15))
                .andExpect(jsonPath("$[2].priorityOrder").value(3))
                .andExpect(jsonPath("$[2].sourceStayId").isEmpty());
    }

    private Room createRoom(int roomNumber) {
        return roomRepository.save(Room.builder()
                .roomNumber(roomNumber)
                .roomStatus(RoomStatus.ACTIVE)
                .build());
    }

    private RoomStay createStay(Room room, LocalDate checkInDate, LocalDate checkOutDate) {
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
