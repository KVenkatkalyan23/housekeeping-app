package com.ibe.housekeeping.task.config;

import com.ibe.housekeeping.allocation.repository.TaskAssignmentRepository;
import com.ibe.housekeeping.common.enums.RoomStatus;
import com.ibe.housekeeping.entity.Room;
import com.ibe.housekeeping.entity.RoomStay;
import com.ibe.housekeeping.room.repository.RoomRepository;
import com.ibe.housekeeping.roomstay.repository.RoomStayRepository;
import com.ibe.housekeeping.task.repository.CleaningTaskRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration
@EnableConfigurationProperties(TaskGenerationBootstrapProperties.class)
public class TaskGenerationBootstrapConfig {

    private static final int START_ROOM_NUMBER = 201;
    private static final int END_ROOM_NUMBER = 212;

    @Bean
    public CommandLineRunner bootstrapTaskGenerationData(
            TaskGenerationBootstrapProperties properties,
            RoomRepository roomRepository,
            RoomStayRepository roomStayRepository,
            TaskAssignmentRepository taskAssignmentRepository,
            CleaningTaskRepository cleaningTaskRepository,
            PlatformTransactionManager transactionManager
    ) {
        return args -> {
            if (!properties.enabled()) {
                return;
            }

            TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
            transactionTemplate.executeWithoutResult(status ->
                    seedVerificationRoomsAndStays(
                            roomRepository,
                            roomStayRepository,
                            taskAssignmentRepository,
                            cleaningTaskRepository
                    )
            );
        };
    }

    protected void seedVerificationRoomsAndStays(
            RoomRepository roomRepository,
            RoomStayRepository roomStayRepository,
            TaskAssignmentRepository taskAssignmentRepository,
            CleaningTaskRepository cleaningTaskRepository
    ) {
        LocalDate today = LocalDate.now();

        Map<Integer, Room> roomsByNumber = roomRepository
                .findAllByRoomNumberBetweenOrderByRoomNumberAsc(START_ROOM_NUMBER, END_ROOM_NUMBER)
                .stream()
                .collect(java.util.stream.Collectors.toMap(Room::getRoomNumber, room -> room));

        for (int roomNumber = START_ROOM_NUMBER; roomNumber <= END_ROOM_NUMBER; roomNumber++) {
            Room room = roomsByNumber.get(roomNumber);
            if (room == null) {
                room = roomRepository.save(Room.builder()
                        .roomNumber(roomNumber)
                        .roomStatus(RoomStatus.ACTIVE)
                        .build());
                roomsByNumber.put(roomNumber, room);
            } else {
                room.setRoomStatus(RoomStatus.ACTIVE);
                roomRepository.save(room);
            }
        }

        List<UUID> roomIds = roomsByNumber.values().stream()
                .map(Room::getId)
                .toList();

        taskAssignmentRepository.deleteAllByCleaningTaskRoomIdIn(roomIds);
        cleaningTaskRepository.deleteAllByRoomIdIn(roomIds);
        roomStayRepository.deleteAllByRoomIdIn(roomIds);

        saveStay(roomStayRepository, roomsByNumber.get(201), today.minusDays(2), today);
        saveStay(roomStayRepository, roomsByNumber.get(202), today.minusDays(1), today.plusDays(1));
        saveStay(roomStayRepository, roomsByNumber.get(203), today, today.plusDays(2));
        saveStay(roomStayRepository, roomsByNumber.get(204), today, today.plusDays(3));
        saveStay(roomStayRepository, roomsByNumber.get(205), today.minusDays(3), today.minusDays(1));
        saveStay(roomStayRepository, roomsByNumber.get(206), today.minusDays(1), today);
        saveStay(roomStayRepository, roomsByNumber.get(207), today.minusDays(2), today.plusDays(3));
        saveStay(roomStayRepository, roomsByNumber.get(208), today, today.plusDays(1));
        saveStay(roomStayRepository, roomsByNumber.get(210), today.plusDays(1), today.plusDays(3));
        saveStay(roomStayRepository, roomsByNumber.get(211), today.minusDays(2), today.plusDays(2));
        saveStay(roomStayRepository, roomsByNumber.get(212), today.minusDays(1), today.plusDays(1));
    }

    private void saveStay(
            RoomStayRepository roomStayRepository,
            Room room,
            LocalDate checkInDate,
            LocalDate checkOutDate
    ) {
        roomStayRepository.save(RoomStay.builder()
                .room(room)
                .checkInDate(checkInDate)
                .checkOutDate(checkOutDate)
                .isCleaned(false)
                .build());
    }
}
