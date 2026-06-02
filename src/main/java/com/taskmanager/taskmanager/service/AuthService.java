package com.taskmanager.taskmanager.service;

import com.taskmanager.taskmanager.dto.AuthResponse;
import com.taskmanager.taskmanager.dto.LoginRequest;
import com.taskmanager.taskmanager.dto.SignupRequest;
import com.taskmanager.taskmanager.exception.ApiException;
import com.taskmanager.taskmanager.model.Role;
import com.taskmanager.taskmanager.model.User;
import com.taskmanager.taskmanager.repository.UserRepository;
import com.taskmanager.taskmanager.security.JwtService;
import com.taskmanager.taskmanager.security.UserPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(
        UserRepository userRepository,
        PasswordEncoder passwordEncoder,
        AuthenticationManager authenticationManager,
        JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    public AuthResponse signup(SignupRequest request) {
        String email = request.email().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new ApiException(HttpStatus.CONFLICT, "Email already registered");
        }

        User user = new User();
        user.setName(request.name().trim());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(request.role() == null ? Role.MEMBER : request.role());
        User saved = userRepository.save(user);
        return response(saved);
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.email().trim().toLowerCase(), request.password())
        );
        User user = userRepository.findByEmail(request.email().trim().toLowerCase())
            .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));
        return response(user);
    }

    private AuthResponse response(User user) {
        String token = jwtService.generateToken(new UserPrincipal(user));
        return new AuthResponse(token, user.getId(), user.getName(), user.getEmail(), user.getRole());
    }
}
