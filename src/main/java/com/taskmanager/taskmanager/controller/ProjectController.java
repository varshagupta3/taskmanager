package com.taskmanager.taskmanager.controller;

import com.taskmanager.taskmanager.dto.MemberRequest;
import com.taskmanager.taskmanager.dto.ProjectRequest;
import com.taskmanager.taskmanager.dto.ProjectResponse;
import com.taskmanager.taskmanager.service.ProjectService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {
    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping
    public List<ProjectResponse> list() {
        return projectService.visibleProjects();
    }

    @PostMapping
    public ProjectResponse create(@Valid @RequestBody ProjectRequest request) {
        return projectService.create(request);
    }

    @PostMapping("/{projectId}/members")
    public ProjectResponse addMember(@PathVariable Long projectId, @Valid @RequestBody MemberRequest request) {
        return projectService.addMember(projectId, request);
    }
}
