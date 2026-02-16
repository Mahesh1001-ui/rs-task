package com.taskboard.exception;

import java.util.UUID;

public class TaskNotFoundException extends RuntimeException {

    public TaskNotFoundException(UUID taskId) {
        super("Task not found with id: " + taskId);
    }

    public TaskNotFoundException(String message) {
        super(message);
    }
}
