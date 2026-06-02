package com.taskmanager.taskmanager.controller;

import com.taskmanager.taskmanager.dto.UserSummary;
import com.taskmanager.taskmanager.repository.UserRepository;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping
    public List<UserSummary> list() {
        return userRepository.findAll().stream()
            .map(UserSummary::from)
            .toList();
    }
}
