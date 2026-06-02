package com.taskmanager.taskmanager.repository;

import com.taskmanager.taskmanager.model.Project;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    @Query("""
        select distinct p from Project p
        left join ProjectMember pm on pm.project = p
        where p.owner.id = :userId or pm.user.id = :userId
        order by p.createdAt desc
        """)
    List<Project> findVisibleToUser(@Param("userId") Long userId);
}
