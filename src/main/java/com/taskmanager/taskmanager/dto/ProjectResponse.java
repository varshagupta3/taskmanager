package com.taskmanager.taskmanager.dto;

import com.taskmanager.taskmanager.model.Project;
import java.time.Instant;
import java.util.List;

public record ProjectResponse(
    Long id,
    String name,
    String description,
    UserSummary owner,
    List<UserSummary> members,
    Instant createdAt
) {
    public static ProjectResponse from(Project project, List<UserSummary> members) {
        return new ProjectResponse(
            project.getId(),
            project.getName(),
            project.getDescription(),
            UserSummary.from(project.getOwner()),
            members,
            project.getCreatedAt()
        );
    }
}
