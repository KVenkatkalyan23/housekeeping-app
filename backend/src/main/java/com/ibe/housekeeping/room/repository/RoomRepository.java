package com.ibe.housekeeping.room.repository;

import com.ibe.housekeeping.common.enums.RoomStatus;
import com.ibe.housekeeping.entity.Room;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomRepository extends JpaRepository<Room, UUID> {

    List<Room> findAllByRoomStatusOrderByRoomNumberAsc(RoomStatus roomStatus);

    List<Room> findAllByRoomNumberBetweenOrderByRoomNumberAsc(Integer startRoomNumber, Integer endRoomNumber);
}
