package com.taskboard.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskboard.dto.BoardRequest;
import com.taskboard.dto.BoardResponse;
import com.taskboard.exception.BoardNotFoundException;
import com.taskboard.service.BoardService;
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
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BoardController.class)
class BoardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BoardService boardService;

    private UUID boardId;
    private BoardResponse testResponse;

    @BeforeEach
    void setUp() {
        boardId = UUID.randomUUID();
        testResponse = BoardResponse.builder()
                .id(boardId)
                .name("Test Board")
                .description("Test Description")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .taskCount(0)
                .build();
    }

    @Test
    @DisplayName("GET /api/boards should return all boards")
    void getAllBoardsShouldReturnAllBoards() throws Exception {
        // Given
        when(boardService.getAllBoards()).thenReturn(Arrays.asList(testResponse));

        // When/Then
        mockMvc.perform(get("/api/boards"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(boardId.toString()))
                .andExpect(jsonPath("$[0].name").value("Test Board"));
    }

    @Test
    @DisplayName("GET /api/boards/{id} should return board when found")
    void getBoardByIdShouldReturnBoardWhenFound() throws Exception {
        // Given
        when(boardService.getBoardById(boardId)).thenReturn(testResponse);

        // When/Then
        mockMvc.perform(get("/api/boards/{id}", boardId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(boardId.toString()))
                .andExpect(jsonPath("$.name").value("Test Board"));
    }

    @Test
    @DisplayName("GET /api/boards/{id} should return 404 when not found")
    void getBoardByIdShouldReturn404WhenNotFound() throws Exception {
        // Given
        when(boardService.getBoardById(boardId))
                .thenThrow(new BoardNotFoundException(boardId));

        // When/Then
        mockMvc.perform(get("/api/boards/{id}", boardId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("POST /api/boards should create board and return 201")
    void createBoardShouldReturnCreated() throws Exception {
        // Given
        BoardRequest request = BoardRequest.builder()
                .name("New Board")
                .description("Description")
                .build();
        when(boardService.createBoard(any(BoardRequest.class))).thenReturn(testResponse);

        // When/Then
        mockMvc.perform(post("/api/boards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Test Board"));
    }

    @Test
    @DisplayName("POST /api/boards should return 400 for invalid request")
    void createBoardShouldReturn400ForInvalidRequest() throws Exception {
        // Given - name is required
        BoardRequest request = BoardRequest.builder()
                .name("")
                .build();

        // When/Then
        mockMvc.perform(post("/api/boards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/boards/{id} should update board")
    void updateBoardShouldReturnUpdatedBoard() throws Exception {
        // Given
        BoardRequest request = BoardRequest.builder()
                .name("Updated Board")
                .description("Updated Description")
                .build();
        when(boardService.updateBoard(eq(boardId), any(BoardRequest.class)))
                .thenReturn(testResponse);

        // When/Then
        mockMvc.perform(put("/api/boards/{id}", boardId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /api/boards/{id} should return 204")
    void deleteBoardShouldReturn204() throws Exception {
        // When/Then
        mockMvc.perform(delete("/api/boards/{id}", boardId))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/boards/{id} should return 404 when not found")
    void deleteBoardShouldReturn404WhenNotFound() throws Exception {
        // Given
        doThrow(new BoardNotFoundException(boardId))
                .when(boardService).deleteBoard(boardId);

        // When/Then
        mockMvc.perform(delete("/api/boards/{id}", boardId))
                .andExpect(status().isNotFound());
    }
}
