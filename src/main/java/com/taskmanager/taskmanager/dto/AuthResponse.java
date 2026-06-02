package com.taskmanager.taskmanager.dto;

import com.taskmanager.taskmanager.model.Role;

public record AuthResponse(String token, Long id, String name, String email, Role role) {
}
