package com.taskboard.service;

import com.taskboard.dto.BoardRequest;
import com.taskboard.dto.BoardResponse;
import com.taskboard.event.BoardEvent;
import com.taskboard.event.BoardEventType;
import com.taskboard.exception.BoardNotFoundException;
import com.taskboard.model.Board;
import com.taskboard.repository.BoardRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BoardServiceTest {

    @Mock
    private BoardRepository boardRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private BoardService boardService;

    @Captor
    private ArgumentCaptor<Board> boardCaptor;

    @Captor
    private ArgumentCaptor<BoardEvent> eventCaptor;

    private Board testBoard;
    private UUID boardId;

    @BeforeEach
    void setUp() {
        boardId = UUID.randomUUID();
        testBoard = Board.builder()
                .id(boardId)
                .name("Test Board")
                .description("Test Description")
                .deleted(false)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    @Nested
    @DisplayName("getAllBoards")
    class GetAllBoards {

        @Test
        @DisplayName("should return all non-deleted boards")
        void shouldReturnAllNonDeletedBoards() {
            // Given
            Board board2 = Board.builder()
                    .id(UUID.randomUUID())
                    .name("Board 2")
                    .deleted(false)
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build();
            when(boardRepository.findByDeletedFalseOrderByCreatedAtDesc())
                    .thenReturn(Arrays.asList(testBoard, board2));

            // When
            List<BoardResponse> result = boardService.getAllBoards();

            // Then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getName()).isEqualTo("Test Board");
            verify(boardRepository).findByDeletedFalseOrderByCreatedAtDesc();
        }

        @Test
        @DisplayName("should return empty list when no boards exist")
        void shouldReturnEmptyListWhenNoBoardsExist() {
            // Given
            when(boardRepository.findByDeletedFalseOrderByCreatedAtDesc())
                    .thenReturn(List.of());

            // When
            List<BoardResponse> result = boardService.getAllBoards();

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("getBoardById")
    class GetBoardById {

        @Test
        @DisplayName("should return board when found")
        void shouldReturnBoardWhenFound() {
            // Given
            when(boardRepository.findByIdAndDeletedFalse(boardId))
                    .thenReturn(Optional.of(testBoard));

            // When
            BoardResponse result = boardService.getBoardById(boardId);

            // Then
            assertThat(result.getId()).isEqualTo(boardId);
            assertThat(result.getName()).isEqualTo("Test Board");
        }

        @Test
        @DisplayName("should throw exception when board not found")
        void shouldThrowExceptionWhenBoardNotFound() {
            // Given
            when(boardRepository.findByIdAndDeletedFalse(boardId))
                    .thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> boardService.getBoardById(boardId))
                    .isInstanceOf(BoardNotFoundException.class)
                    .hasMessageContaining(boardId.toString());
        }
    }

    @Nested
    @DisplayName("createBoard")
    class CreateBoard {

        @Test
        @DisplayName("should create board and publish event")
        void shouldCreateBoardAndPublishEvent() {
            // Given
            BoardRequest request = BoardRequest.builder()
                    .name("New Board")
                    .description("New Description")
                    .build();
            when(boardRepository.save(any(Board.class))).thenReturn(testBoard);

            // When
            BoardResponse result = boardService.createBoard(request);

            // Then
            assertThat(result).isNotNull();
            verify(boardRepository).save(boardCaptor.capture());
            verify(eventPublisher).publishEvent(eventCaptor.capture());

            Board capturedBoard = boardCaptor.getValue();
            assertThat(capturedBoard.getName()).isEqualTo("New Board");

            BoardEvent capturedEvent = eventCaptor.getValue();
            assertThat(capturedEvent.getType()).isEqualTo(BoardEventType.BOARD_CREATED);
        }
    }

    @Nested
    @DisplayName("updateBoard")
    class UpdateBoard {

        @Test
        @DisplayName("should update board and publish event")
        void shouldUpdateBoardAndPublishEvent() {
            // Given
            BoardRequest request = BoardRequest.builder()
                    .name("Updated Board")
                    .description("Updated Description")
                    .build();
            when(boardRepository.findByIdAndDeletedFalse(boardId))
                    .thenReturn(Optional.of(testBoard));
            when(boardRepository.save(any(Board.class))).thenReturn(testBoard);

            // When
            BoardResponse result = boardService.updateBoard(boardId, request);

            // Then
            verify(boardRepository).save(boardCaptor.capture());
            verify(eventPublisher).publishEvent(eventCaptor.capture());

            Board capturedBoard = boardCaptor.getValue();
            assertThat(capturedBoard.getName()).isEqualTo("Updated Board");

            BoardEvent capturedEvent = eventCaptor.getValue();
            assertThat(capturedEvent.getType()).isEqualTo(BoardEventType.BOARD_UPDATED);
        }

        @Test
        @DisplayName("should throw exception when board not found")
        void shouldThrowExceptionWhenBoardNotFound() {
            // Given
            BoardRequest request = BoardRequest.builder().name("Test").build();
            when(boardRepository.findByIdAndDeletedFalse(boardId))
                    .thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> boardService.updateBoard(boardId, request))
                    .isInstanceOf(BoardNotFoundException.class);
            verify(boardRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("deleteBoard")
    class DeleteBoard {

        @Test
        @DisplayName("should soft delete board by setting deleted flag")
        void shouldSoftDeleteBoard() {
            // Given
            when(boardRepository.findByIdAndDeletedFalse(boardId))
                    .thenReturn(Optional.of(testBoard));

            // When
            boardService.deleteBoard(boardId);

            // Then
            // BUG #1: This test verifies soft delete behavior
            // The implementation should set deleted=true and save, not delete
            verify(boardRepository).save(boardCaptor.capture());
            Board capturedBoard = boardCaptor.getValue();
            assertThat(capturedBoard.isDeleted()).isTrue();

            verify(eventPublisher).publishEvent(eventCaptor.capture());
            assertThat(eventCaptor.getValue().getType()).isEqualTo(BoardEventType.BOARD_DELETED);

            // Should NOT call delete
            verify(boardRepository, never()).delete(any(Board.class));
        }

        @Test
        @DisplayName("should throw exception when board not found")
        void shouldThrowExceptionWhenBoardNotFound() {
            // Given
            when(boardRepository.findByIdAndDeletedFalse(boardId))
                    .thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> boardService.deleteBoard(boardId))
                    .isInstanceOf(BoardNotFoundException.class);
        }
    }
}
