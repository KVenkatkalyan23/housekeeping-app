package com.ibe.housekeeping.task.repository;

import com.ibe.housekeeping.common.enums.TaskStatus;
import com.ibe.housekeeping.entity.CleaningTask;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CleaningTaskRepository extends JpaRepository<CleaningTask, UUID> {

    boolean existsByRoomIdAndTaskDate(UUID roomId, LocalDate taskDate);

    List<CleaningTask> findAllByTaskDateOrderByPriorityOrderAscRoomRoomNumberAsc(LocalDate taskDate);

    List<CleaningTask> findAllByTaskDateAndTaskStatusOrderByPriorityOrderAscRoomRoomNumberAsc(
            LocalDate taskDate,
            TaskStatus taskStatus
    );

    void deleteAllByRoomIdIn(Collection<UUID> roomIds);
}
