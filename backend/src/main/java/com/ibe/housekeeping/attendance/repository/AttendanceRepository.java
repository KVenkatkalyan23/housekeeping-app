package com.ibe.housekeeping.attendance.repository;

import com.ibe.housekeeping.entity.Attendance;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttendanceRepository extends JpaRepository<Attendance, UUID> {

    List<Attendance> findAllByStaffIdAndClockOutTimeIsNull(UUID staffId);

    Optional<Attendance> findTopByStaffIdAndClockOutTimeIsNullOrderByClockInTimeDesc(UUID staffId);

    Optional<Attendance> findTopByStaffIdAndWorkDateOrderByClockInTimeDesc(UUID staffId, LocalDate workDate);

    List<Attendance> findAllByStaffIdAndWorkDateBetweenOrderByWorkDateAscClockInTimeAsc(
            UUID staffId,
            LocalDate startDate,
            LocalDate endDate
    );

    Optional<Attendance> findTopByStaffIdOrderByWorkDateAscClockInTimeAsc(UUID staffId);
}
