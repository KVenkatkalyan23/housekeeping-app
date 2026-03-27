package com.ibe.housekeeping.allocation.repository;

import com.ibe.housekeeping.entity.TaskAssignment;
import java.util.Collection;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TaskAssignmentRepository extends JpaRepository<TaskAssignment, UUID> {

    void deleteAllByCleaningTaskRoomIdIn(Collection<UUID> roomIds);

    @Query("""
            select assignment
            from TaskAssignment assignment
            join fetch assignment.cleaningTask task
            join fetch task.room room
            join fetch task.shift shift
            left join fetch assignment.staff staff
            where task.taskDate = :taskDate
              and shift.id = :shiftId
            order by task.priorityOrder asc, room.roomNumber asc, task.id asc
            """)
    List<TaskAssignment> findAllByTaskDateAndShiftId(
            @Param("taskDate") LocalDate taskDate,
            @Param("shiftId") UUID shiftId
    );

    @Query("""
            select assignment
            from TaskAssignment assignment
            join fetch assignment.cleaningTask task
            join fetch task.room room
            join fetch task.shift shift
            left join fetch assignment.staff staff
            left join fetch staff.preferredShift preferredShift
            where task.taskDate = :taskDate
            order by task.priorityOrder asc, room.roomNumber asc, task.id asc
            """)
    List<TaskAssignment> findAllByTaskDate(
            @Param("taskDate") LocalDate taskDate
    );

    @Query("""
            select assignment
            from TaskAssignment assignment
            join fetch assignment.cleaningTask task
            join fetch task.room room
            join fetch task.shift shift
            left join fetch assignment.staff staff
            left join fetch staff.preferredShift preferredShift
            where task.taskDate = :taskDate
              and shift.id in :shiftIds
            order by task.priorityOrder asc, room.roomNumber asc, task.id asc
            """)
    List<TaskAssignment> findAllByTaskDateAndShiftIds(
            @Param("taskDate") LocalDate taskDate,
            @Param("shiftIds") List<UUID> shiftIds
    );

    @Query("""
            select assignment
            from TaskAssignment assignment
            join fetch assignment.cleaningTask task
            join fetch task.room room
            left join fetch task.shift shift
            left join fetch task.sourceStay sourceStay
            where assignment.staff.id = :staffId
              and task.taskDate = :taskDate
            order by task.priorityOrder asc, room.roomNumber asc, task.id asc
            """)
    List<TaskAssignment> findAllByStaffIdAndTaskDate(
            @Param("staffId") UUID staffId,
            @Param("taskDate") LocalDate taskDate
    );

    @Query("""
            select assignment
            from TaskAssignment assignment
            join fetch assignment.cleaningTask task
            where task.id = :taskId
              and assignment.staff.id = :staffId
            """)
    Optional<TaskAssignment> findByCleaningTaskIdAndStaffId(
            @Param("taskId") UUID taskId,
            @Param("staffId") UUID staffId
    );
}
