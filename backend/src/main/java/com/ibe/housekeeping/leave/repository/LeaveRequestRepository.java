package com.ibe.housekeeping.leave.repository;

import com.ibe.housekeeping.common.enums.LeaveStatus;
import com.ibe.housekeeping.entity.LeaveRequest;
import java.time.LocalDate;
import java.util.Collection;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, UUID> {

    Page<LeaveRequest> findByStaffUserId(UUID userId, Pageable pageable);

    Page<LeaveRequest> findAllByOrderByRequestedAtDesc(Pageable pageable);

    boolean existsByStaffIdAndStatusAndLeaveStartDateLessThanEqualAndLeaveEndDateGreaterThanEqual(
            UUID staffId,
            LeaveStatus status,
            java.time.LocalDate leaveEndDate,
            java.time.LocalDate leaveStartDate
    );

    @Query("""
            select leaveRequest
            from LeaveRequest leaveRequest
            join fetch leaveRequest.staff staff
            where leaveRequest.status = :status
              and staff.id in :staffIds
              and leaveRequest.leaveStartDate <= :today
              and leaveRequest.leaveEndDate >= :today
            """)
    java.util.List<LeaveRequest> findActiveApprovedLeavesForStaffIds(
            @Param("staffIds") Collection<UUID> staffIds,
            @Param("status") LeaveStatus status,
            @Param("today") LocalDate today
    );
}
