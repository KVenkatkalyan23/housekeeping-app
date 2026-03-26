package com.ibe.housekeeping.allocation.repository;

import com.ibe.housekeeping.entity.TaskAssignment;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TaskAssignmentRepository extends JpaRepository<TaskAssignment, UUID> {

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
            where task.taskDate = :taskDate
              and shift.id in :shiftIds
            order by task.priorityOrder asc, room.roomNumber asc, task.id asc
            """)
    List<TaskAssignment> findAllByTaskDateAndShiftIds(
            @Param("taskDate") LocalDate taskDate,
            @Param("shiftIds") List<UUID> shiftIds
    );
}
