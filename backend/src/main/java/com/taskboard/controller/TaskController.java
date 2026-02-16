package com.taskboard.controller;

import com.taskboard.dto.TaskMoveRequest;
import com.taskboard.dto.TaskRequest;
import com.taskboard.dto.TaskResponse;
import com.taskboard.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @GetMapping("/boards/{boardId}/tasks")
    public ResponseEntity<List<TaskResponse>> getTasksByBoardId(@PathVariable UUID boardId) {
        return ResponseEntity.ok(taskService.getTasksByBoardId(boardId));
    }

    @GetMapping("/tasks/{taskId}")
    public ResponseEntity<TaskResponse> getTaskById(@PathVariable UUID taskId) {
        return ResponseEntity.ok(taskService.getTaskById(taskId));
    }

    @PostMapping("/boards/{boardId}/tasks")
    public ResponseEntity<TaskResponse> createTask(
            @PathVariable UUID boardId,
            @Valid @RequestBody TaskRequest request) {
        TaskResponse response = taskService.createTask(boardId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/tasks/{taskId}")
    public ResponseEntity<TaskResponse> updateTask(
            @PathVariable UUID taskId,
            @Valid @RequestBody TaskRequest request) {
        return ResponseEntity.ok(taskService.updateTask(taskId, request));
    }

    @PatchMapping("/tasks/{taskId}/move")
    public ResponseEntity<TaskResponse> moveTask(
            @PathVariable UUID taskId,
            @Valid @RequestBody TaskMoveRequest request) {
        return ResponseEntity.ok(taskService.moveTask(taskId, request));
    }

    @DeleteMapping("/tasks/{taskId}")
    public ResponseEntity<Void> deleteTask(@PathVariable UUID taskId) {
        taskService.deleteTask(taskId);
        return ResponseEntity.noContent().build();
    }
}
