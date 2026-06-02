package com.taskmanager.taskmanager.dto;

import com.taskmanager.taskmanager.model.TaskItem;
import com.taskmanager.taskmanager.model.TaskStatus;
import java.time.Instant;
import java.time.LocalDate;

public record TaskResponse(
    Long id,
    String title,
    String description,
    TaskStatus status,
    LocalDate dueDate,
    Long projectId,
    String projectName,
    UserSummary assignedTo,
    UserSummary createdBy,
    boolean overdue,
    Instant createdAt
) {
    public static TaskResponse from(TaskItem task) {
        boolean overdue = task.getDueDate() != null
            && task.getDueDate().isBefore(LocalDate.now())
            && task.getStatus() != TaskStatus.DONE;
        return new TaskResponse(
            task.getId(),
            task.getTitle(),
            task.getDescription(),
            task.getStatus(),
            task.getDueDate(),
            task.getProject().getId(),
            task.getProject().getName(),
            UserSummary.from(task.getAssignedTo()),
            UserSummary.from(task.getCreatedBy()),
            overdue,
            task.getCreatedAt()
        );
    }
}
