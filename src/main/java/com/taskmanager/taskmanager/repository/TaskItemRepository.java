package com.taskmanager.taskmanager.repository;

import com.taskmanager.taskmanager.model.TaskItem;
import com.taskmanager.taskmanager.model.TaskStatus;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TaskItemRepository extends JpaRepository<TaskItem, Long> {
    @Query("""
        select t from TaskItem t
        left join ProjectMember pm on pm.project = t.project
        where t.project.owner.id = :userId or pm.user.id = :userId or t.assignedTo.id = :userId
        order by t.createdAt desc
        """)
    List<TaskItem> findVisibleToUser(@Param("userId") Long userId);

    List<TaskItem> findByProjectIdOrderByCreatedAtDesc(Long projectId);

    long countByAssignedToIdAndStatusNot(Long userId, TaskStatus status);

    long countByAssignedToIdAndStatus(Long userId, TaskStatus status);

    long countByAssignedToIdAndDueDateBeforeAndStatusNot(Long userId, LocalDate date, TaskStatus status);
}
