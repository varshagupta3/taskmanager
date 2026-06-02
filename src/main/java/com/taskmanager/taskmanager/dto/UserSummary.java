package com.taskmanager.taskmanager.dto;

import com.taskmanager.taskmanager.model.Role;
import com.taskmanager.taskmanager.model.User;

public record UserSummary(Long id, String name, String email, Role role) {
    public static UserSummary from(User user) {
        if (user == null) {
            return null;
        }
        return new UserSummary(user.getId(), user.getName(), user.getEmail(), user.getRole());
    }
}
