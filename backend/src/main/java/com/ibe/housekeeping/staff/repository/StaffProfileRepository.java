package com.ibe.housekeeping.staff.repository;

import com.ibe.housekeeping.entity.StaffProfile;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StaffProfileRepository extends JpaRepository<StaffProfile, UUID> {

    Optional<StaffProfile> findByUserId(UUID userId);

    Optional<StaffProfile> findByUserUsername(String username);

    List<StaffProfile> findAllByOrderByIdAsc();

    List<StaffProfile> findAllByPreferredShiftIdOrderByIdAsc(UUID shiftId);
}
