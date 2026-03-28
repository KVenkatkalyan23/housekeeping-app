package com.ibe.housekeeping.staff.repository;

import com.ibe.housekeeping.entity.StaffProfile;
import com.ibe.housekeeping.common.enums.AvailabilityStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StaffProfileRepository extends JpaRepository<StaffProfile, UUID> {

    Optional<StaffProfile> findByUserId(UUID userId);

    Optional<StaffProfile> findByUserUsername(String username);

    List<StaffProfile> findAllByOrderByIdAsc();

    List<StaffProfile> findAllByPreferredShiftIdOrderByIdAsc(UUID shiftId);

    @Query("""
            select staff
            from StaffProfile staff
            left join fetch staff.preferredShift preferredShift
            where staff.availabilityStatus = :availabilityStatus
            order by staff.id asc
            """)
    List<StaffProfile> findAllByAvailabilityStatusWithPreferredShift(
            @Param("availabilityStatus") AvailabilityStatus availabilityStatus
    );
}
