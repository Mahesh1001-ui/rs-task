package com.taskboard.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskboard.dto.TaskMoveRequest;
import com.taskboard.dto.TaskRequest;
import com.taskboard.dto.TaskResponse;
import com.taskboard.exception.InvalidStatusTransitionException;
import com.taskboard.exception.TaskNotFoundException;
import com.taskboard.model.TaskPriority;
import com.taskboard.model.TaskStatus;
import com.taskboard.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Arrays;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaskController.class)
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TaskService taskService;

    private UUID boardId;
    private UUID taskId;
    private TaskResponse testResponse;

    @BeforeEach
    void setUp() {
        boardId = UUID.randomUUID();
        taskId = UUID.randomUUID();
        testResponse = TaskResponse.builder()
                .id(taskId)
                .boardId(boardId)
                .title("Test Task")
                .description("Test Description")
                .status(TaskStatus.TODO)
                .priority(TaskPriority.MEDIUM)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    @Test
    @DisplayName("GET /api/boards/{boardId}/tasks should return all tasks")
    void getTasksByBoardIdShouldReturnAllTasks() throws Exception {
        // Given
        when(taskService.getTasksByBoardId(boardId))
                .thenReturn(Arrays.asList(testResponse));

        // When/Then
        mockMvc.perform(get("/api/boards/{boardId}/tasks", boardId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(taskId.toString()))
                .andExpect(jsonPath("$[0].title").value("Test Task"));
    }

    @Test
    @DisplayName("POST /api/boards/{boardId}/tasks should create task and return 201")
    void createTaskShouldReturnCreated() throws Exception {
        // Given
        TaskRequest request = TaskRequest.builder()
                .title("New Task")
                .description("Description")
                .priority(TaskPriority.HIGH)
                .build();
        when(taskService.createTask(eq(boardId), any(TaskRequest.class)))
                .thenReturn(testResponse);

        // When/Then
        mockMvc.perform(post("/api/boards/{boardId}/tasks", boardId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Test Task"));
    }

    @Test
    @DisplayName("POST /api/boards/{boardId}/tasks should return 400 for invalid request")
    void createTaskShouldReturn400ForInvalidRequest() throws Exception {
        // Given - title is required
        TaskRequest request = TaskRequest.builder()
                .title("")
                .build();

        // When/Then
        mockMvc.perform(post("/api/boards/{boardId}/tasks", boardId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PATCH /api/tasks/{taskId}/move should move task")
    void moveTaskShouldReturnUpdatedTask() throws Exception {
        // Given
        TaskMoveRequest request = TaskMoveRequest.builder()
                .status(TaskStatus.IN_PROGRESS)
                .build();
        testResponse.setStatus(TaskStatus.IN_PROGRESS);
        when(taskService.moveTask(eq(taskId), any(TaskMoveRequest.class)))
                .thenReturn(testResponse);

        // When/Then
        mockMvc.perform(patch("/api/tasks/{taskId}/move", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    @DisplayName("PATCH /api/tasks/{taskId}/move should return 400 for invalid transition")
    void moveTaskShouldReturn400ForInvalidTransition() throws Exception {
        // Given
        TaskMoveRequest request = TaskMoveRequest.builder()
                .status(TaskStatus.DONE)
                .build();
        when(taskService.moveTask(eq(taskId), any(TaskMoveRequest.class)))
                .thenThrow(new InvalidStatusTransitionException(TaskStatus.TODO, TaskStatus.DONE));

        // When/Then
        mockMvc.perform(patch("/api/tasks/{taskId}/move", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid status transition from TODO to DONE"));
    }

    @Test
    @DisplayName("PUT /api/tasks/{taskId} should update task")
    void updateTaskShouldReturnUpdatedTask() throws Exception {
        // Given
        TaskRequest request = TaskRequest.builder()
                .title("Updated Task")
                .description("Updated Description")
                .priority(TaskPriority.HIGH)
                .build();
        when(taskService.updateTask(eq(taskId), any(TaskRequest.class)))
                .thenReturn(testResponse);

        // When/Then
        mockMvc.perform(put("/api/tasks/{taskId}", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /api/tasks/{taskId} should return 204")
    void deleteTaskShouldReturn204() throws Exception {
        // When/Then
        mockMvc.perform(delete("/api/tasks/{taskId}", taskId))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("GET /api/tasks/{taskId} should return 404 when not found")
    void getTaskByIdShouldReturn404WhenNotFound() throws Exception {
        // Given
        when(taskService.getTaskById(taskId))
                .thenThrow(new TaskNotFoundException(taskId));

        // When/Then
        mockMvc.perform(get("/api/tasks/{taskId}", taskId))
                .andExpect(status().isNotFound());
    }
}
