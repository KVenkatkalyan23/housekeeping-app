package com.ibe.housekeeping.roomstay.repository;

import com.ibe.housekeeping.entity.RoomStay;
import java.util.Collection;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomStayRepository extends JpaRepository<RoomStay, UUID> {

    Optional<RoomStay> findTopByRoomIdAndCheckInDateLessThanEqualAndCheckOutDateGreaterThanEqualOrderByCheckOutDateDescCreatedAtDesc(
            UUID roomId,
            LocalDate checkInDate,
            LocalDate checkOutDate
    );

    long countByCheckInDateLessThanEqualAndCheckOutDateGreaterThanEqual(LocalDate checkInDate, LocalDate checkOutDate);

    void deleteAllByRoomIdIn(Collection<UUID> roomIds);
}
