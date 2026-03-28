package com.ibe.housekeeping.task.repository;

import com.ibe.housekeeping.common.enums.TaskStatus;
import com.ibe.housekeeping.entity.CleaningTask;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CleaningTaskRepository extends JpaRepository<CleaningTask, UUID> {

    boolean existsByRoomIdAndTaskDate(UUID roomId, LocalDate taskDate);

    List<CleaningTask> findAllByTaskDateOrderByPriorityOrderAscRoomRoomNumberAsc(LocalDate taskDate);

    List<CleaningTask> findAllByTaskDateAndTaskStatusOrderByPriorityOrderAscRoomRoomNumberAsc(
            LocalDate taskDate,
            TaskStatus taskStatus
    );

    @Query("""
            select task
            from CleaningTask task
            left join TaskAssignment assignment on assignment.cleaningTask = task
            join fetch task.room room
            left join fetch task.shift shift
            where task.taskDate = :taskDate
              and assignment.id is null
              and task.taskStatus not in :excludedStatuses
            order by task.priorityOrder asc, room.roomNumber asc, task.id asc
            """)
    List<CleaningTask> findUnassignedEligibleTasksForAllocation(
            @Param("taskDate") LocalDate taskDate,
            @Param("excludedStatuses") Collection<TaskStatus> excludedStatuses
    );

    @Query("""
            select task
            from CleaningTask task
            left join TaskAssignment assignment on assignment.cleaningTask = task
            join fetch task.room room
            left join fetch task.shift shift
            where task.taskDate = :taskDate
              and assignment.id is null
              and task.taskStatus not in :excludedStatuses
            order by task.priorityOrder asc, room.roomNumber asc, task.id asc
            """)
    List<CleaningTask> findUnassignedEligibleTasksForResult(
            @Param("taskDate") LocalDate taskDate,
            @Param("excludedStatuses") Collection<TaskStatus> excludedStatuses
    );

    @Query("""
            select task
            from CleaningTask task
            left join fetch task.shift shift
            where task.taskDate = :taskDate
            order by task.priorityOrder asc, task.id asc
            """)
    List<CleaningTask> findDashboardTasksByTaskDate(@Param("taskDate") LocalDate taskDate);

    void deleteAllByRoomIdIn(Collection<UUID> roomIds);
}
