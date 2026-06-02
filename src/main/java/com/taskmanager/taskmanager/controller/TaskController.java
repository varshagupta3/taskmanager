package com.taskmanager.taskmanager.controller;

import com.taskmanager.taskmanager.dto.DashboardResponse;
import com.taskmanager.taskmanager.dto.StatusRequest;
import com.taskmanager.taskmanager.dto.TaskRequest;
import com.taskmanager.taskmanager.dto.TaskResponse;
import com.taskmanager.taskmanager.service.TaskService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class TaskController {
    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping("/tasks")
    public List<TaskResponse> list() {
        return taskService.visibleTasks();
    }

    @PostMapping("/tasks")
    public TaskResponse create(@Valid @RequestBody TaskRequest request) {
        return taskService.create(request);
    }

    @PatchMapping("/tasks/{taskId}/status")
    public TaskResponse updateStatus(@PathVariable Long taskId, @Valid @RequestBody StatusRequest request) {
        return taskService.updateStatus(taskId, request);
    }

    @GetMapping("/projects/{projectId}/tasks")
    public List<TaskResponse> projectTasks(@PathVariable Long projectId) {
        return taskService.projectTasks(projectId);
    }

    @GetMapping("/dashboard")
    public DashboardResponse dashboard() {
        return taskService.dashboard();
    }
}
