package com.taskmanager.taskmanager.service;

import com.taskmanager.taskmanager.dto.DashboardResponse;
import com.taskmanager.taskmanager.dto.StatusRequest;
import com.taskmanager.taskmanager.dto.TaskRequest;
import com.taskmanager.taskmanager.dto.TaskResponse;
import com.taskmanager.taskmanager.exception.ApiException;
import com.taskmanager.taskmanager.model.Project;
import com.taskmanager.taskmanager.model.Role;
import com.taskmanager.taskmanager.model.TaskItem;
import com.taskmanager.taskmanager.model.TaskStatus;
import com.taskmanager.taskmanager.model.User;
import com.taskmanager.taskmanager.repository.ProjectRepository;
import com.taskmanager.taskmanager.repository.TaskItemRepository;
import com.taskmanager.taskmanager.repository.UserRepository;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TaskService {
    private final TaskItemRepository taskItemRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProjectService projectService;
    private final CurrentUserService currentUserService;

    public TaskService(
        TaskItemRepository taskItemRepository,
        ProjectRepository projectRepository,
        UserRepository userRepository,
        ProjectService projectService,
        CurrentUserService currentUserService
    ) {
        this.taskItemRepository = taskItemRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.projectService = projectService;
        this.currentUserService = currentUserService;
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> visibleTasks() {
        User current = currentUserService.get();
        List<TaskItem> tasks;
        if (current.getRole() == Role.ADMIN) {
            tasks = taskItemRepository.findAll();
        } else {
            tasks = taskItemRepository.findVisibleToUser(current.getId());
        }
        return tasks.stream()
            .map(TaskResponse::from)
            .toList();
    }

    @Transactional
    public TaskResponse create(TaskRequest request) {
        User current = requireAdmin();
        Project project = projectRepository.findById(request.projectId())
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Project not found"));
        User assignee = null;
        if (request.assignedToId() != null) {
            assignee = userRepository.findById(request.assignedToId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Assigned user not found"));
            if (!projectService.isProjectMember(project.getId(), assignee.getId())) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Assigned user must be a project member");
            }
        }

        TaskItem task = new TaskItem();
        task.setTitle(request.title().trim());
        task.setDescription(request.description());
        task.setProject(project);
        task.setCreatedBy(current);
        task.setAssignedTo(assignee);
        task.setDueDate(request.dueDate());
        return TaskResponse.from(taskItemRepository.save(task));
    }

    @Transactional
    public TaskResponse updateStatus(Long taskId, StatusRequest request) {
        User current = currentUserService.get();
        TaskItem task = task(taskId);
        boolean assignedToCurrent = task.getAssignedTo() != null && task.getAssignedTo().getId().equals(current.getId());
        if (current.getRole() != Role.ADMIN && !assignedToCurrent) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Only admins or the assigned member can update status");
        }
        task.setStatus(request.status());
        return TaskResponse.from(taskItemRepository.save(task));
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> projectTasks(Long projectId) {
        projectService.requireVisibleProject(projectId);
        return taskItemRepository.findByProjectIdOrderByCreatedAtDesc(projectId).stream()
            .map(TaskResponse::from)
            .toList();
    }

    @Transactional(readOnly = true)
    public DashboardResponse dashboard() {
        User current = currentUserService.get();
        List<TaskItem> visibleTasks = current.getRole() == Role.ADMIN
            ? taskItemRepository.findAll()
            : taskItemRepository.findVisibleToUser(current.getId());
        long visibleProjects = projectService.visibleProjects().size();
        return new DashboardResponse(
            taskItemRepository.countByAssignedToIdAndStatusNot(current.getId(), TaskStatus.DONE),
            taskItemRepository.countByAssignedToIdAndStatus(current.getId(), TaskStatus.DONE),
            taskItemRepository.countByAssignedToIdAndDueDateBeforeAndStatusNot(current.getId(), LocalDate.now(), TaskStatus.DONE),
            visibleProjects,
            visibleTasks.size()
        );
    }

    private User requireAdmin() {
        User current = currentUserService.get();
        if (current.getRole() != Role.ADMIN) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Admin role required");
        }
        return current;
    }

    private TaskItem task(Long taskId) {
        return taskItemRepository.findById(taskId)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Task not found"));
    }
}
