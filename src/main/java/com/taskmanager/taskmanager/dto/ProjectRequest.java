package com.taskmanager.taskmanager.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProjectRequest(
    @NotBlank String name,
    @Size(max = 1200) String description
) {
}
