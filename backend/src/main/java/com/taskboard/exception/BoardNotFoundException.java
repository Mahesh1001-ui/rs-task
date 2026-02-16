package com.taskboard.exception;

import java.util.UUID;

public class BoardNotFoundException extends RuntimeException {

    public BoardNotFoundException(UUID boardId) {
        super("Board not found with id: " + boardId);
    }

    public BoardNotFoundException(String message) {
        super(message);
    }
}
