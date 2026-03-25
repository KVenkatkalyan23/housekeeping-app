package com.ibe.housekeeping.leave.service;

import com.ibe.housekeeping.auth.repository.UserRepository;
import com.ibe.housekeeping.entity.LeaveRequest;
import com.ibe.housekeeping.entity.StaffProfile;
import com.ibe.housekeeping.entity.User;
import com.ibe.housekeeping.leave.dto.LeaveHistoryItemResponse;
import com.ibe.housekeeping.leave.repository.LeaveRequestRepository;
import com.ibe.housekeeping.staff.repository.StaffProfileRepository;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class LeaveHistoryService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final StaffProfileRepository staffProfileRepository;
    private final UserRepository userRepository;

    public LeaveHistoryService(
            LeaveRequestRepository leaveRequestRepository,
            StaffProfileRepository staffProfileRepository,
            UserRepository userRepository
    ) {
        this.leaveRequestRepository = leaveRequestRepository;
        this.staffProfileRepository = staffProfileRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<LeaveHistoryItemResponse> getLeaveHistory(String username) {
        StaffProfile staffProfile = loadStaffProfile(username);

        return leaveRequestRepository.findByStaffIdOrderByLeaveStartDateDesc(staffProfile.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private StaffProfile loadStaffProfile(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required"));

        return staffProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Staff profile not found for this user."));
    }

    private LeaveHistoryItemResponse toResponse(LeaveRequest leaveRequest) {
        long durationDays = 1;

        if (leaveRequest.getLeaveStartDate() != null && leaveRequest.getLeaveEndDate() != null) {
            durationDays = Math.max(
                    ChronoUnit.DAYS.between(leaveRequest.getLeaveStartDate(), leaveRequest.getLeaveEndDate()) + 1,
                    1
            );
        }

        return new LeaveHistoryItemResponse(
                leaveRequest.getId(),
                leaveRequest.getLeaveType(),
                leaveRequest.getLeaveStartDate(),
                leaveRequest.getLeaveEndDate(),
                leaveRequest.getStatus(),
                durationDays,
                leaveRequest.getReason(),
                leaveRequest.getRequestedAt()
        );
    }
}
