package com.taskmanager.taskmanager.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record TaskRequest(
    @NotBlank String title,
    @Size(max = 1600) String description,
    @NotNull Long projectId,
    Long assignedToId,
    LocalDate dueDate
) {
}
