package com.taskmanager.taskmanager.dto;

public record DashboardResponse(long assignedOpen, long completed, long overdue, long visibleProjects, long visibleTasks) {
}
