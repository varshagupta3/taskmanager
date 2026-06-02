package com.taskmanager.taskmanager.repository;

import com.taskmanager.taskmanager.model.Project;
import com.taskmanager.taskmanager.model.ProjectMember;
import com.taskmanager.taskmanager.model.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Long> {
    boolean existsByProjectIdAndUserId(Long projectId, Long userId);

    Optional<ProjectMember> findByProjectAndUser(Project project, User user);

    List<ProjectMember> findByProjectId(Long projectId);
}
