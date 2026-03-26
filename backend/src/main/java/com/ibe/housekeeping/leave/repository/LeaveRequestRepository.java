package com.ibe.housekeeping.leave.repository;

import com.ibe.housekeeping.entity.LeaveRequest;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, UUID> {

    List<LeaveRequest> findByStaffIdOrderByLeaveStartDateDesc(UUID staffId);

    boolean existsByStaffIdAndStatusIgnoreCaseAndLeaveStartDateLessThanEqualAndLeaveEndDateGreaterThanEqual(
            UUID staffId,
            String status,
            java.time.LocalDate leaveEndDate,
            java.time.LocalDate leaveStartDate
    );
}
