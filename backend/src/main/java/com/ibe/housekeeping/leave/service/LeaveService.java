package com.ibe.housekeeping.leave.service;

import com.ibe.housekeeping.auth.repository.UserRepository;
import com.ibe.housekeeping.allocation.service.ReallocationService;
import com.ibe.housekeeping.common.enums.LeaveStatus;
import com.ibe.housekeeping.common.enums.LeaveType;
import com.ibe.housekeeping.common.enums.Role;
import com.ibe.housekeeping.entity.LeaveRequest;
import com.ibe.housekeeping.entity.StaffProfile;
import com.ibe.housekeeping.entity.User;
import com.ibe.housekeeping.leave.dto.AdminLeavesResponse;
import com.ibe.housekeeping.leave.dto.ApplyLeaveRequest;
import com.ibe.housekeeping.leave.dto.LeaveListItemResponse;
import com.ibe.housekeeping.leave.dto.LeavePaginationResponse;
import com.ibe.housekeeping.leave.dto.MyLeavesResponse;
import com.ibe.housekeeping.leave.repository.LeaveRequestRepository;
import com.ibe.housekeeping.staff.repository.StaffProfileRepository;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

@Service
public class LeaveService {

    private static final int MAX_LEAVE_DAYS = 7;

    private final LeaveRequestRepository leaveRequestRepository;
    private final StaffProfileRepository staffProfileRepository;
    private final UserRepository userRepository;
    private final ReallocationService reallocationService;

    public LeaveService(
            LeaveRequestRepository leaveRequestRepository,
            StaffProfileRepository staffProfileRepository,
            UserRepository userRepository,
            ReallocationService reallocationService
    ) {
        this.leaveRequestRepository = leaveRequestRepository;
        this.staffProfileRepository = staffProfileRepository;
        this.userRepository = userRepository;
        this.reallocationService = reallocationService;
    }

    @Transactional
    public LeaveListItemResponse applyLeave(String authenticatedUsername, ApplyLeaveRequest request) {
        User authenticatedUser = loadUserByUsername(authenticatedUsername);
        User targetUser = loadUserById(request.userId());

        if (!authenticatedUser.getId().equals(targetUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only apply leave for your own account.");
        }

        StaffProfile staffProfile = staffProfileRepository.findByUserId(targetUser.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Staff profile not found for this user."));

        LocalDate fromDate = parseDate(request.fromDate(), "fromDate");
        LocalDate toDate = parseDate(request.toDate(), "toDate");
        LeaveType leaveType = parseLeaveType(request.leaveType());

        validateDates(fromDate, toDate, leaveType);

        LeaveRequest leaveRequest = leaveRequestRepository.save(LeaveRequest.builder()
                .staff(staffProfile)
                .leaveStartDate(fromDate)
                .leaveEndDate(toDate)
                .leaveType(leaveType)
                .reason(normalizeReason(request.reason()))
                .status(LeaveStatus.APPROVED)
                .build());

        if (!fromDate.isAfter(LocalDate.now()) && !toDate.isBefore(LocalDate.now())) {
            reallocationService.reallocateTodayForStaff(staffProfile.getId());
        }

        return toItemResponse(leaveRequest);
    }

    @Transactional(readOnly = true)
    public MyLeavesResponse getMyLeaves(String authenticatedUsername, String userId, int page, int size) {
        User authenticatedUser = loadUserByUsername(authenticatedUsername);
        User targetUser = loadUserById(userId);

        if (!authenticatedUser.getId().equals(targetUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only view your own leaves.");
        }

        Page<LeaveRequest> leavePage = leaveRequestRepository.findByStaffUserId(targetUser.getId(), buildPageable(page, size));
        return new MyLeavesResponse(
                leavePage.getContent().stream().map(this::toItemResponse).toList(),
                toPagination(leavePage)
        );
    }

    @Transactional(readOnly = true)
    public AdminLeavesResponse getAdminLeaves(String authenticatedUsername, int page, int size) {
        User authenticatedUser = loadUserByUsername(authenticatedUsername);

        if (authenticatedUser.getRole() != Role.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only admins can view all leave requests.");
        }

        Page<LeaveRequest> leavePage = leaveRequestRepository.findAllByOrderByRequestedAtDesc(buildPageable(page, size));
        return new AdminLeavesResponse(
                leavePage.getContent().stream().map(this::toItemResponse).toList(),
                toPagination(leavePage)
        );
    }

    private void validateDates(LocalDate fromDate, LocalDate toDate, LeaveType leaveType) {
        LocalDate today = LocalDate.now();

        if (fromDate.isBefore(today)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "fromDate must be today or future.");
        }

        if (toDate.isBefore(today)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "toDate must be today or future.");
        }

        if (toDate.isBefore(fromDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "toDate must be on or after fromDate.");
        }

        long durationDays = calculateDurationDays(fromDate, toDate);
        if (durationDays > MAX_LEAVE_DAYS) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, leaveType + " leave cannot exceed 7 days.");
        }

        if (leaveType == LeaveType.PLANNED && fromDate.isBefore(today.plusDays(2))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "PLANNED leave must be applied at least 2 days before fromDate.");
        }
    }

    private LeaveListItemResponse toItemResponse(LeaveRequest leaveRequest) {
        StaffProfile staff = leaveRequest.getStaff();
        User user = staff.getUser();

        return new LeaveListItemResponse(
                leaveRequest.getId(),
                user.getId(),
                user.getUsername(),
                staff.getId(),
                staff.getFullName(),
                leaveRequest.getLeaveStartDate(),
                leaveRequest.getLeaveEndDate(),
                leaveRequest.getLeaveType(),
                leaveRequest.getStatus(),
                calculateDurationDays(leaveRequest.getLeaveStartDate(), leaveRequest.getLeaveEndDate()),
                leaveRequest.getReason(),
                leaveRequest.getRequestedAt()
        );
    }

    private LeavePaginationResponse toPagination(Page<LeaveRequest> leavePage) {
        return new LeavePaginationResponse(
                leavePage.getNumber(),
                leavePage.getSize(),
                leavePage.getTotalElements(),
                leavePage.getTotalPages(),
                leavePage.hasPrevious(),
                leavePage.hasNext()
        );
    }

    private Pageable buildPageable(int page, int size) {
        if (page < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "page must be 0 or greater.");
        }

        if (size < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "size must be at least 1.");
        }

        return PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "requestedAt"));
    }

    private User loadUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required"));
    }

    private User loadUserById(String userId) {
        UUID parsedUserId = parseUuid(userId, "userId");
        return userRepository.findById(parsedUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "userId does not exist."));
    }

    private UUID parseUuid(String value, String fieldName) {
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, fieldName + " must be a valid UUID.");
        }
    }

    private LocalDate parseDate(String value, String fieldName) {
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, fieldName + " must be a valid ISO date.");
        }
    }

    private LeaveType parseLeaveType(String value) {
        try {
            return LeaveType.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException | NullPointerException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "leaveType must be SICK or PLANNED.");
        }
    }

    private String normalizeReason(String reason) {
        return StringUtils.hasText(reason) ? reason.trim() : null;
    }

    private long calculateDurationDays(LocalDate fromDate, LocalDate toDate) {
        return ChronoUnit.DAYS.between(fromDate, toDate) + 1;
    }
}
