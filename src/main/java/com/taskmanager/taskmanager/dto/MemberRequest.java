package com.taskmanager.taskmanager.dto;

import jakarta.validation.constraints.NotNull;

public record MemberRequest(@NotNull Long userId) {
}
