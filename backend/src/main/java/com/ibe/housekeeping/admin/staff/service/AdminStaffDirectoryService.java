package com.ibe.housekeeping.admin.staff.service;

import com.ibe.housekeeping.admin.staff.dto.AdminStaffDirectoryItemResponse;
import com.ibe.housekeeping.admin.staff.dto.AdminStaffDirectoryResponse;
import com.ibe.housekeeping.admin.staff.dto.StaffDirectorySummaryResponse;
import com.ibe.housekeeping.common.enums.AvailabilityStatus;
import com.ibe.housekeeping.common.enums.LeaveStatus;
import com.ibe.housekeeping.common.enums.LeaveType;
import com.ibe.housekeeping.entity.LeaveRequest;
import com.ibe.housekeeping.entity.StaffProfile;
import com.ibe.housekeeping.leave.repository.LeaveRequestRepository;
import com.ibe.housekeeping.staff.repository.StaffProfileRepository;
import java.time.Clock;
import java.time.LocalDate;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AdminStaffDirectoryService {

    private final StaffProfileRepository staffProfileRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final Clock clock;

    public AdminStaffDirectoryService(
            StaffProfileRepository staffProfileRepository,
            LeaveRequestRepository leaveRequestRepository,
            Clock clock
    ) {
        this.staffProfileRepository = staffProfileRepository;
        this.leaveRequestRepository = leaveRequestRepository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public AdminStaffDirectoryResponse getStaffDirectory(int page, int size, String search, String status) {
        validatePagination(page, size);
        DirectoryStatus requestedStatus = parseStatus(status);
        String normalizedSearch = normalizeSearch(search);
        LocalDate today = LocalDate.now(clock);

        List<StaffProfile> allStaff = staffProfileRepository.findAllForAdminDirectory();
        Map<UUID, DirectoryStatus> allStatuses = resolveStatuses(allStaff, today);

        List<StaffProfile> searchedStaff = normalizedSearch == null
                ? allStaff
                : staffProfileRepository.searchAllForAdminDirectory(normalizedSearch);
        Map<UUID, DirectoryStatus> searchedStatuses = normalizedSearch == null
                ? allStatuses
                : resolveStatuses(searchedStaff, today);

        List<AdminStaffDirectoryItemResponse> filteredItems = searchedStaff.stream()
                .map(staff -> Map.entry(staff, searchedStatuses.getOrDefault(staff.getId(), DirectoryStatus.OFF_DUTY)))
                .filter(entry -> requestedStatus == DirectoryStatus.ALL || entry.getValue() == requestedStatus)
                .map(entry -> toItemResponse(entry.getKey(), entry.getValue()))
                .toList();

        int fromIndex = Math.min(page * size, filteredItems.size());
        int toIndex = Math.min(fromIndex + size, filteredItems.size());
        int totalPages = filteredItems.isEmpty() ? 0 : (int) Math.ceil(filteredItems.size() / (double) size);

        return new AdminStaffDirectoryResponse(
                filteredItems.subList(fromIndex, toIndex),
                page,
                size,
                filteredItems.size(),
                totalPages,
                buildSummary(allStatuses)
        );
    }

    private Map<UUID, DirectoryStatus> resolveStatuses(List<StaffProfile> staffProfiles, LocalDate today) {
        if (staffProfiles.isEmpty()) {
            return Map.of();
        }

        List<UUID> staffIds = staffProfiles.stream()
                .map(StaffProfile::getId)
                .toList();

        Map<UUID, LeaveType> activeLeaveTypes = leaveRequestRepository.findActiveApprovedLeavesForStaffIds(
                        staffIds,
                        LeaveStatus.APPROVED,
                        today
                ).stream()
                .collect(Collectors.toMap(
                        leaveRequest -> leaveRequest.getStaff().getId(),
                        LeaveRequest::getLeaveType,
                        this::mergeLeaveTypes
                ));

        return staffProfiles.stream()
                .collect(Collectors.toMap(
                        StaffProfile::getId,
                        staffProfile -> resolveStaffStatus(staffProfile, activeLeaveTypes)
                ));
    }

    private DirectoryStatus resolveStaffStatus(StaffProfile staffProfile, Map<UUID, LeaveType> activeLeaveTypes) {
        LeaveType activeLeaveType = activeLeaveTypes.get(staffProfile.getId());
        if (activeLeaveType == LeaveType.SICK) {
            return DirectoryStatus.SICK;
        }

        if (activeLeaveType != null) {
            return DirectoryStatus.LEAVE;
        }

        return staffProfile.getAvailabilityStatus() == AvailabilityStatus.ON_DUTY
                ? DirectoryStatus.ON_DUTY
                : DirectoryStatus.OFF_DUTY;
    }

    private StaffDirectorySummaryResponse buildSummary(Map<UUID, DirectoryStatus> allStatuses) {
        Map<DirectoryStatus, Long> counts = allStatuses.values().stream()
                .collect(Collectors.groupingBy(
                        Function.identity(),
                        () -> new EnumMap<>(DirectoryStatus.class),
                        Collectors.counting()
                ));

        return new StaffDirectorySummaryResponse(
                allStatuses.size(),
                counts.getOrDefault(DirectoryStatus.ON_DUTY, 0L),
                counts.getOrDefault(DirectoryStatus.OFF_DUTY, 0L),
                counts.getOrDefault(DirectoryStatus.LEAVE, 0L),
                counts.getOrDefault(DirectoryStatus.SICK, 0L)
        );
    }

    private AdminStaffDirectoryItemResponse toItemResponse(StaffProfile staffProfile, DirectoryStatus status) {
        return new AdminStaffDirectoryItemResponse(
                staffProfile.getId(),
                buildStaffCode(staffProfile.getId()),
                staffProfile.getFullName(),
                StringUtils.hasText(staffProfile.getEmail()) ? staffProfile.getEmail().trim() : null,
                StringUtils.hasText(staffProfile.getPhone()) ? staffProfile.getPhone().trim() : null,
                status.name(),
                staffProfile.getUser() != null ? staffProfile.getUser().getId() : null,
                null
        );
    }

    private LeaveType mergeLeaveTypes(LeaveType left, LeaveType right) {
        return left == LeaveType.SICK || right != LeaveType.SICK ? left : right;
    }

    private String buildStaffCode(UUID staffId) {
        String value = staffId.toString().replace("-", "").toUpperCase(Locale.ENGLISH);
        return "STF-" + value.substring(0, 4);
    }

    private void validatePagination(int page, int size) {
        if (page < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "page must be 0 or greater.");
        }

        if (size < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "size must be at least 1.");
        }
    }

    private DirectoryStatus parseStatus(String status) {
        if (!StringUtils.hasText(status)) {
            return DirectoryStatus.ALL;
        }

        try {
            return DirectoryStatus.valueOf(status.trim().toUpperCase(Locale.ENGLISH));
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "status must be one of ALL, ON_DUTY, OFF_DUTY, LEAVE, or SICK."
            );
        }
    }

    private String normalizeSearch(String search) {
        return StringUtils.hasText(search) ? search.trim() : null;
    }

    private enum DirectoryStatus {
        ALL,
        ON_DUTY,
        OFF_DUTY,
        LEAVE,
        SICK
    }
}
