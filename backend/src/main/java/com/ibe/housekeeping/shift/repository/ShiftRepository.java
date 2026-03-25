package com.ibe.housekeeping.shift.repository;

import com.ibe.housekeeping.entity.Shift;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShiftRepository extends JpaRepository<Shift, UUID> {

    Optional<Shift> findByShiftCode(String shiftCode);
}
