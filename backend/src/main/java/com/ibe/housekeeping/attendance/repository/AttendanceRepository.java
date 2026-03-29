package com.ibe.housekeeping.attendance.repository;

import com.ibe.housekeeping.entity.Attendance;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    long countByWorkDate(LocalDate workDate);

    long countByWorkDateBetween(LocalDate startDate, LocalDate endDate);

    @EntityGraph(attributePaths = {"staff", "staff.user", "shift"})
    @Query("""
            select a
            from Attendance a
            join a.staff s
            join a.shift sh
            where a.workDate between :fromDate and :toDate
            order by a.workDate desc, a.clockInTime desc, s.fullName asc
            """)
    List<Attendance> findAllForAdminSummaryByWorkDateBetween(
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );

    @EntityGraph(attributePaths = {"staff", "staff.user", "shift"})
    @Query("""
            select a
            from Attendance a
            join a.staff s
            join s.user u
            join a.shift sh
            where a.workDate between :fromDate and :toDate
              and (
                lower(s.fullName) like lower(concat('%', :search, '%'))
                or lower(u.username) like lower(concat('%', :search, '%'))
                or lower(sh.shiftName) like lower(concat('%', :search, '%'))
                or lower(sh.shiftCode) like lower(concat('%', :search, '%'))
              )
            order by a.workDate desc, a.clockInTime desc, s.fullName asc
            """)
    Page<Attendance> findAllForAdminLogsByWorkDateBetweenAndSearch(
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("search") String search,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {"staff", "staff.user", "shift"})
    @Query("""
            select a
            from Attendance a
            join a.staff s
            where a.workDate between :fromDate and :toDate
            order by a.workDate desc, a.clockInTime desc, s.fullName asc
            """)
    Page<Attendance> findAllForAdminLogsByWorkDateBetween(
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            Pageable pageable
    );
}
