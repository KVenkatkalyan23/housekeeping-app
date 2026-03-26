package com.ibe.housekeeping.shift.repository;

import com.ibe.housekeeping.entity.Shift;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShiftRepository extends JpaRepository<Shift, UUID> {

    Optional<Shift> findByShiftCode(String shiftCode);

    List<Shift> findAllByOrderByStartTimeAscShiftCodeAsc();

    Optional<Shift> findByStartTimeAndEndTime(LocalTime startTime, LocalTime endTime);
}
