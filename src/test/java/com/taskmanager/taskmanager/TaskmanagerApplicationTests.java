package com.taskmanager.taskmanager;

import static org.assertj.core.api.Assertions.assertThat;

import com.taskmanager.taskmanager.dto.ProjectRequest;
import com.taskmanager.taskmanager.dto.SignupRequest;
import com.taskmanager.taskmanager.model.Role;
import com.taskmanager.taskmanager.service.AuthService;
import com.taskmanager.taskmanager.service.ProjectService;
import com.taskmanager.taskmanager.repository.UserRepository;
import com.taskmanager.taskmanager.model.User;
import com.taskmanager.taskmanager.security.UserPrincipal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class TaskmanagerApplicationTests {

    @Autowired
    private AuthService authService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private UserRepository userRepository;

    @Test
    void contextLoads() {
    }

    @Test
    void testAdminCanSeeAllProjects() {
        // Create an Admin user
        SignupRequest adminSignup = new SignupRequest("Admin User", "admin@taskmanager.com", "password", Role.ADMIN);
        authService.signup(adminSignup);
        User admin = userRepository.findByEmail("admin@taskmanager.com").orElseThrow();

        // Create a Member user
        SignupRequest memberSignup = new SignupRequest("Member User", "member@taskmanager.com", "password", Role.MEMBER);
        authService.signup(memberSignup);
        User member = userRepository.findByEmail("member@taskmanager.com").orElseThrow();

        // Authenticate as Admin
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(new UserPrincipal(admin), null, null)
        );

        // Create a project as Admin
        projectService.create(new ProjectRequest("Admin Project", "Description"));

        // Authenticate as Member
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(new UserPrincipal(member), null, null)
        );

        // Member should not see the project (as they are not added as a member)
        assertThat(projectService.visibleProjects()).isEmpty();

        // Authenticate as a different Admin
        SignupRequest admin2Signup = new SignupRequest("Admin User 2", "admin2@taskmanager.com", "password", Role.ADMIN);
        authService.signup(admin2Signup);
        User admin2 = userRepository.findByEmail("admin2@taskmanager.com").orElseThrow();
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(new UserPrincipal(admin2), null, null)
        );

        // Admin 2 should see the project even though they are not members/owners of it
        assertThat(projectService.visibleProjects()).hasSize(1);
    }
}
