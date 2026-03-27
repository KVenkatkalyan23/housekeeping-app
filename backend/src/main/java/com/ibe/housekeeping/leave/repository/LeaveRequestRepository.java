package com.ibe.housekeeping.leave.repository;

import com.ibe.housekeeping.common.enums.LeaveStatus;
import com.ibe.housekeeping.entity.LeaveRequest;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, UUID> {

    Page<LeaveRequest> findByStaffUserId(UUID userId, Pageable pageable);

    Page<LeaveRequest> findAllByOrderByRequestedAtDesc(Pageable pageable);

    boolean existsByStaffIdAndStatusAndLeaveStartDateLessThanEqualAndLeaveEndDateGreaterThanEqual(
            UUID staffId,
            LeaveStatus status,
            java.time.LocalDate leaveEndDate,
            java.time.LocalDate leaveStartDate
    );
}
