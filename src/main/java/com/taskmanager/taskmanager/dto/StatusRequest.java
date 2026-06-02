package com.taskmanager.taskmanager.dto;

import com.taskmanager.taskmanager.model.TaskStatus;
import jakarta.validation.constraints.NotNull;

public record StatusRequest(@NotNull TaskStatus status) {
}
