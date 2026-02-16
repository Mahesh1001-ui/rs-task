package com.taskboard.service;

import com.taskboard.dto.TaskMoveRequest;
import com.taskboard.dto.TaskRequest;
import com.taskboard.dto.TaskResponse;
import com.taskboard.event.TaskEvent;
import com.taskboard.event.TaskEventType;
import com.taskboard.exception.BoardNotFoundException;
import com.taskboard.exception.InvalidStatusTransitionException;
import com.taskboard.exception.TaskNotFoundException;
import com.taskboard.model.Board;
import com.taskboard.model.Task;
import com.taskboard.model.TaskPriority;
import com.taskboard.model.TaskStatus;
import com.taskboard.repository.BoardRepository;
import com.taskboard.repository.TaskRepository;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private BoardRepository boardRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private TaskService taskService;

    @Captor
    private ArgumentCaptor<Task> taskCaptor;

    @Captor
    private ArgumentCaptor<TaskEvent> eventCaptor;

    private Board testBoard;
    private Task testTask;
    private UUID boardId;
    private UUID taskId;

    @BeforeEach
    void setUp() {
        boardId = UUID.randomUUID();
        taskId = UUID.randomUUID();

        testBoard = Board.builder()
                .id(boardId)
                .name("Test Board")
                .deleted(false)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        testTask = Task.builder()
                .id(taskId)
                .title("Test Task")
                .description("Test Description")
                .status(TaskStatus.TODO)
                .priority(TaskPriority.MEDIUM)
                .board(testBoard)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    @Nested
    @DisplayName("getTasksByBoardId")
    class GetTasksByBoardId {

        @Test
        @DisplayName("should return all tasks for a board")
        void shouldReturnAllTasksForBoard() {
            // Given
            when(boardRepository.existsById(boardId)).thenReturn(true);
            when(taskRepository.findByBoardIdOrderByCreatedAtDesc(boardId))
                    .thenReturn(List.of(testTask));

            // When
            List<TaskResponse> result = taskService.getTasksByBoardId(boardId);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getTitle()).isEqualTo("Test Task");
        }

        @Test
        @DisplayName("should throw exception when board not found")
        void shouldThrowExceptionWhenBoardNotFound() {
            // Given
            when(boardRepository.existsById(boardId)).thenReturn(false);

            // When/Then
            assertThatThrownBy(() -> taskService.getTasksByBoardId(boardId))
                    .isInstanceOf(BoardNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("createTask")
    class CreateTask {

        @Test
        @DisplayName("should create task with default status TODO")
        void shouldCreateTaskWithDefaultStatusTodo() {
            // Given
            TaskRequest request = TaskRequest.builder()
                    .title("New Task")
                    .description("New Description")
                    .priority(TaskPriority.HIGH)
                    .build();
            when(boardRepository.findByIdAndDeletedFalse(boardId))
                    .thenReturn(Optional.of(testBoard));
            when(taskRepository.save(any(Task.class))).thenReturn(testTask);

            // When
            TaskResponse result = taskService.createTask(boardId, request);

            // Then
            verify(taskRepository).save(taskCaptor.capture());
            Task capturedTask = taskCaptor.getValue();
            assertThat(capturedTask.getBoard()).isEqualTo(testBoard);

            verify(eventPublisher).publishEvent(eventCaptor.capture());
            assertThat(eventCaptor.getValue().getType()).isEqualTo(TaskEventType.TASK_CREATED);
        }

        @Test
        @DisplayName("should throw exception when board not found")
        void shouldThrowExceptionWhenBoardNotFound() {
            // Given
            TaskRequest request = TaskRequest.builder().title("Test").build();
            when(boardRepository.findByIdAndDeletedFalse(boardId))
                    .thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> taskService.createTask(boardId, request))
                    .isInstanceOf(BoardNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("moveTask")
    class MoveTask {

        @Test
        @DisplayName("should move task from TODO to IN_PROGRESS")
        void shouldMoveTaskFromTodoToInProgress() {
            // Given
            testTask.setStatus(TaskStatus.TODO);
            TaskMoveRequest request = TaskMoveRequest.builder()
                    .status(TaskStatus.IN_PROGRESS)
                    .build();
            when(taskRepository.findById(taskId)).thenReturn(Optional.of(testTask));
            when(taskRepository.save(any(Task.class))).thenReturn(testTask);

            // When
            TaskResponse result = taskService.moveTask(taskId, request);

            // Then
            verify(taskRepository).save(taskCaptor.capture());
            assertThat(taskCaptor.getValue().getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);

            verify(eventPublisher).publishEvent(eventCaptor.capture());
            TaskEvent event = eventCaptor.getValue();
            assertThat(event.getType()).isEqualTo(TaskEventType.TASK_MOVED);
            assertThat(event.getPreviousStatus()).isEqualTo(TaskStatus.TODO);
            assertThat(event.getNewStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
        }

        @Test
        @DisplayName("should move task from IN_PROGRESS to DONE")
        void shouldMoveTaskFromInProgressToDone() {
            // Given
            testTask.setStatus(TaskStatus.IN_PROGRESS);
            TaskMoveRequest request = TaskMoveRequest.builder()
                    .status(TaskStatus.DONE)
                    .build();
            when(taskRepository.findById(taskId)).thenReturn(Optional.of(testTask));
            when(taskRepository.save(any(Task.class))).thenReturn(testTask);

            // When
            taskService.moveTask(taskId, request);

            // Then
            verify(taskRepository).save(taskCaptor.capture());
            assertThat(taskCaptor.getValue().getStatus()).isEqualTo(TaskStatus.DONE);
        }

        @Test
        @DisplayName("should reject invalid status transition from TODO to DONE")
        void shouldRejectInvalidStatusTransition() {
            // Given
            testTask.setStatus(TaskStatus.TODO);
            TaskMoveRequest request = TaskMoveRequest.builder()
                    .status(TaskStatus.DONE)
                    .build();
            when(taskRepository.findById(taskId)).thenReturn(Optional.of(testTask));

            // When/Then
            // BUG #3: This test expects validation to prevent TODO -> DONE
            // The implementation currently allows any transition
            assertThatThrownBy(() -> taskService.moveTask(taskId, request))
                    .isInstanceOf(InvalidStatusTransitionException.class)
                    .hasMessageContaining("Invalid status transition");

            // Task should not be saved if validation fails
            verify(taskRepository, never()).save(any(Task.class));
        }

        @Test
        @DisplayName("should allow moving task back from DONE to IN_PROGRESS")
        void shouldAllowMovingBackFromDoneToInProgress() {
            // Given
            testTask.setStatus(TaskStatus.DONE);
            TaskMoveRequest request = TaskMoveRequest.builder()
                    .status(TaskStatus.IN_PROGRESS)
                    .build();
            when(taskRepository.findById(taskId)).thenReturn(Optional.of(testTask));
            when(taskRepository.save(any(Task.class))).thenReturn(testTask);

            // When
            taskService.moveTask(taskId, request);

            // Then
            verify(taskRepository).save(taskCaptor.capture());
            assertThat(taskCaptor.getValue().getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
        }

        @Test
        @DisplayName("should allow moving task back from IN_PROGRESS to TODO")
        void shouldAllowMovingBackFromInProgressToTodo() {
            // Given
            testTask.setStatus(TaskStatus.IN_PROGRESS);
            TaskMoveRequest request = TaskMoveRequest.builder()
                    .status(TaskStatus.TODO)
                    .build();
            when(taskRepository.findById(taskId)).thenReturn(Optional.of(testTask));
            when(taskRepository.save(any(Task.class))).thenReturn(testTask);

            // When
            taskService.moveTask(taskId, request);

            // Then
            verify(taskRepository).save(taskCaptor.capture());
            assertThat(taskCaptor.getValue().getStatus()).isEqualTo(TaskStatus.TODO);
        }

        @Test
        @DisplayName("should throw exception when task not found")
        void shouldThrowExceptionWhenTaskNotFound() {
            // Given
            TaskMoveRequest request = TaskMoveRequest.builder()
                    .status(TaskStatus.IN_PROGRESS)
                    .build();
            when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> taskService.moveTask(taskId, request))
                    .isInstanceOf(TaskNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("updateTask")
    class UpdateTask {

        @Test
        @DisplayName("should update task and publish event")
        void shouldUpdateTaskAndPublishEvent() {
            // Given
            TaskRequest request = TaskRequest.builder()
                    .title("Updated Title")
                    .description("Updated Description")
                    .priority(TaskPriority.HIGH)
                    .build();
            when(taskRepository.findById(taskId)).thenReturn(Optional.of(testTask));
            when(taskRepository.save(any(Task.class))).thenReturn(testTask);

            // When
            taskService.updateTask(taskId, request);

            // Then
            verify(taskRepository).save(taskCaptor.capture());
            Task captured = taskCaptor.getValue();
            assertThat(captured.getTitle()).isEqualTo("Updated Title");
            assertThat(captured.getPriority()).isEqualTo(TaskPriority.HIGH);

            verify(eventPublisher).publishEvent(eventCaptor.capture());
            assertThat(eventCaptor.getValue().getType()).isEqualTo(TaskEventType.TASK_UPDATED);
        }
    }

    @Nested
    @DisplayName("deleteTask")
    class DeleteTask {

        @Test
        @DisplayName("should delete task and publish event")
        void shouldDeleteTaskAndPublishEvent() {
            // Given
            when(taskRepository.findById(taskId)).thenReturn(Optional.of(testTask));

            // When
            taskService.deleteTask(taskId);

            // Then
            verify(taskRepository).delete(testTask);
            verify(eventPublisher).publishEvent(eventCaptor.capture());
            TaskEvent event = eventCaptor.getValue();
            assertThat(event.getType()).isEqualTo(TaskEventType.TASK_DELETED);
            assertThat(event.getPayload().getId()).isEqualTo(taskId);
        }

        @Test
        @DisplayName("should throw exception when task not found")
        void shouldThrowExceptionWhenTaskNotFound() {
            // Given
            when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> taskService.deleteTask(taskId))
                    .isInstanceOf(TaskNotFoundException.class);
        }
    }
}
