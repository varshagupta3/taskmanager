package com.taskmanager.taskmanager.service;

import com.taskmanager.taskmanager.dto.MemberRequest;
import com.taskmanager.taskmanager.dto.ProjectRequest;
import com.taskmanager.taskmanager.dto.ProjectResponse;
import com.taskmanager.taskmanager.dto.UserSummary;
import com.taskmanager.taskmanager.exception.ApiException;
import com.taskmanager.taskmanager.model.Project;
import com.taskmanager.taskmanager.model.ProjectMember;
import com.taskmanager.taskmanager.model.Role;
import com.taskmanager.taskmanager.model.User;
import com.taskmanager.taskmanager.repository.ProjectMemberRepository;
import com.taskmanager.taskmanager.repository.ProjectRepository;
import com.taskmanager.taskmanager.repository.UserRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;

    public ProjectService(
        ProjectRepository projectRepository,
        ProjectMemberRepository projectMemberRepository,
        UserRepository userRepository,
        CurrentUserService currentUserService
    ) {
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.userRepository = userRepository;
        this.currentUserService = currentUserService;
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> visibleProjects() {
        User current = currentUserService.get();
        List<Project> projects;
        if (current.getRole() == Role.ADMIN) {
            projects = projectRepository.findAll();
        } else {
            projects = projectRepository.findVisibleToUser(current.getId());
        }
        return projects.stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional
    public ProjectResponse create(ProjectRequest request) {
        User current = requireAdmin();
        Project project = new Project();
        project.setName(request.name().trim());
        project.setDescription(request.description());
        project.setOwner(current);
        Project saved = projectRepository.save(project);
        addMember(saved, current);
        return toResponse(saved);
    }

    @Transactional
    public ProjectResponse addMember(Long projectId, MemberRequest request) {
        requireAdmin();
        Project project = project(projectId);
        User user = userRepository.findById(request.userId())
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
        addMember(project, user);
        return toResponse(project);
    }

    @Transactional(readOnly = true)
    public Project requireVisibleProject(Long projectId) {
        User current = currentUserService.get();
        Project project = project(projectId);
        if (current.getRole() == Role.ADMIN
            || project.getOwner().getId().equals(current.getId())
            || projectMemberRepository.existsByProjectIdAndUserId(projectId, current.getId())) {
            return project;
        }
        throw new ApiException(HttpStatus.FORBIDDEN, "You do not have access to this project");
    }

    public boolean isProjectMember(Long projectId, Long userId) {
        Project project = project(projectId);
        return project.getOwner().getId().equals(userId)
            || projectMemberRepository.existsByProjectIdAndUserId(projectId, userId);
    }

    private void addMember(Project project, User user) {
        if (projectMemberRepository.existsByProjectIdAndUserId(project.getId(), user.getId())) {
            return;
        }
        ProjectMember member = new ProjectMember();
        member.setProject(project);
        member.setUser(user);
        projectMemberRepository.save(member);
    }

    private User requireAdmin() {
        User current = currentUserService.get();
        if (current.getRole() != Role.ADMIN) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Admin role required");
        }
        return current;
    }

    private Project project(Long projectId) {
        return projectRepository.findById(projectId)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Project not found"));
    }

    private ProjectResponse toResponse(Project project) {
        List<UserSummary> members = projectMemberRepository.findByProjectId(project.getId()).stream()
            .map(ProjectMember::getUser)
            .map(UserSummary::from)
            .toList();
        return ProjectResponse.from(project, members);
    }
}
