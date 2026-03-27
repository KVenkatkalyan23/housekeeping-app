package com.ibe.housekeeping.attendance.service;

import com.ibe.housekeeping.attendance.dto.AttendanceStatusResponse;
import com.ibe.housekeeping.attendance.dto.ClockInResponse;
import com.ibe.housekeeping.attendance.dto.ClockOutResponse;
import com.ibe.housekeeping.attendance.repository.AttendanceRepository;
import com.ibe.housekeeping.allocation.service.ReallocationService;
import com.ibe.housekeeping.auth.repository.UserRepository;
import com.ibe.housekeeping.common.enums.AvailabilityStatus;
import com.ibe.housekeeping.entity.Attendance;
import com.ibe.housekeeping.entity.Shift;
import com.ibe.housekeeping.entity.StaffProfile;
import com.ibe.housekeeping.entity.User;
import com.ibe.housekeeping.staff.repository.StaffProfileRepository;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final StaffProfileRepository staffProfileRepository;
    private final UserRepository userRepository;
    private final ReallocationService reallocationService;

    public AttendanceService(
            AttendanceRepository attendanceRepository,
            StaffProfileRepository staffProfileRepository,
            UserRepository userRepository,
            ReallocationService reallocationService
    ) {
        this.attendanceRepository = attendanceRepository;
        this.staffProfileRepository = staffProfileRepository;
        this.userRepository = userRepository;
        this.reallocationService = reallocationService;
    }

    @Transactional(readOnly = true)
    public AttendanceStatusResponse getCurrentAttendance(String username) {
        StaffProfile staffProfile = loadStaffProfile(username);
        Attendance activeAttendance = loadSingleActiveAttendance(staffProfile, true);
        return toStatusResponse(staffProfile, activeAttendance);
    }

    @Transactional
    public ClockInResponse clockIn(String username) {
        StaffProfile staffProfile = loadStaffProfile(username);
        Shift shift = requirePreferredShift(staffProfile);

        Attendance activeAttendance = loadSingleActiveAttendance(staffProfile, true);
        if (activeAttendance != null) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "You are already clocked in. Please clock out before clocking in again."
            );
        }

        LocalDateTime clockInTime = LocalDateTime.now();
        Attendance attendance = Attendance.builder()
                .staff(staffProfile)
                .shift(shift)
                .workDate(clockInTime.toLocalDate())
                .clockInTime(clockInTime)
                .workedMinutes(0)
                .build();

        Attendance savedAttendance = attendanceRepository.save(attendance);
        staffProfile.setAvailabilityStatus(AvailabilityStatus.ON_DUTY);
        staffProfileRepository.save(staffProfile);

        return new ClockInResponse(
                savedAttendance.getId(),
                staffProfile.getId(),
                shift.getId(),
                savedAttendance.getWorkDate(),
                savedAttendance.getClockInTime(),
                savedAttendance.getWorkedMinutes(),
                staffProfile.getAvailabilityStatus()
        );
    }

    @Transactional
    public ClockOutResponse clockOut(String username) {
        StaffProfile staffProfile = loadStaffProfile(username);
        Attendance activeAttendance = loadSingleActiveAttendance(staffProfile, true);

        if (activeAttendance == null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No active clock-in found for today.");
        }

        LocalDateTime clockOutTime = LocalDateTime.now();
        if (activeAttendance.getClockInTime() != null && clockOutTime.isBefore(activeAttendance.getClockInTime())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Clock-out time cannot be earlier than clock-in time.");
        }

        int workedMinutes = calculateWorkedMinutes(activeAttendance.getClockInTime(), clockOutTime);

        activeAttendance.setClockOutTime(clockOutTime);
        activeAttendance.setWorkedMinutes(workedMinutes);
        Attendance savedAttendance = attendanceRepository.save(activeAttendance);

        staffProfile.setAvailabilityStatus(AvailabilityStatus.OFF_DUTY);
        staffProfileRepository.save(staffProfile);
        reallocationService.reallocateTodayForStaff(staffProfile.getId());

        return new ClockOutResponse(
                savedAttendance.getId(),
                staffProfile.getId(),
                savedAttendance.getShift().getId(),
                savedAttendance.getWorkDate(),
                savedAttendance.getClockInTime(),
                savedAttendance.getClockOutTime(),
                savedAttendance.getWorkedMinutes(),
                staffProfile.getAvailabilityStatus()
        );
    }

    private StaffProfile loadStaffProfile(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required"));

        return staffProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Staff profile not found for this user."));
    }

    private Shift requirePreferredShift(StaffProfile staffProfile) {
        Shift shift = staffProfile.getPreferredShift();
        if (shift == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No shift assigned to this staff profile.");
        }
        return shift;
    }

    private Attendance loadSingleActiveAttendance(StaffProfile staffProfile, boolean failOnMultiple) {
        List<Attendance> openAttendances = attendanceRepository.findAllByStaffIdAndClockOutTimeIsNull(staffProfile.getId());

        if (openAttendances.isEmpty()) {
            return null;
        }

        if (openAttendances.size() > 1 && failOnMultiple) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Multiple active attendance records were found. Please contact an administrator."
            );
        }

        return attendanceRepository.findTopByStaffIdAndClockOutTimeIsNullOrderByClockInTimeDesc(staffProfile.getId())
                .orElse(null);
    }

    private AttendanceStatusResponse toStatusResponse(StaffProfile staffProfile, Attendance activeAttendance) {
        Shift shift = staffProfile.getPreferredShift();

        return new AttendanceStatusResponse(
                activeAttendance != null,
                activeAttendance != null ? activeAttendance.getId() : null,
                staffProfile.getId(),
                shift != null ? shift.getId() : null,
                shift != null ? shift.getShiftCode() : null,
                shift != null ? shift.getShiftName() : null,
                shift != null ? shift.getStartTime() : null,
                shift != null ? shift.getEndTime() : null,
                activeAttendance != null ? activeAttendance.getWorkDate() : LocalDate.now(),
                activeAttendance != null ? activeAttendance.getClockInTime() : null,
                activeAttendance != null ? activeAttendance.getClockOutTime() : null,
                activeAttendance != null ? activeAttendance.getWorkedMinutes() : 0,
                staffProfile.getAvailabilityStatus()
        );
    }

    private int calculateWorkedMinutes(LocalDateTime clockInTime, LocalDateTime clockOutTime) {
        if (clockInTime == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Active attendance record is missing clock-in time.");
        }

        long durationMinutes = Duration.between(clockInTime, clockOutTime).toMinutes();

        if (durationMinutes < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Worked minutes cannot be negative.");
        }

        return Math.toIntExact(durationMinutes);
    }
}
