package com.taskmanager.taskmanager.service;

import com.taskmanager.taskmanager.model.User;
import com.taskmanager.taskmanager.security.UserPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {
    public User get() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ((UserPrincipal) principal).getUser();
    }
}
