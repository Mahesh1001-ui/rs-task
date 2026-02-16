package com.taskboard.event;

import com.taskboard.dto.TaskResponse;
import com.taskboard.model.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskEvent {

    private TaskEventType type;
    private TaskResponse payload;
    private UUID boardId;
    private TaskStatus previousStatus;
    private TaskStatus newStatus;
    private Instant timestamp;

    public static TaskEvent created(TaskResponse task) {
        return TaskEvent.builder()
                .type(TaskEventType.TASK_CREATED)
                .payload(task)
                .boardId(task.getBoardId())
                .timestamp(Instant.now())
                .build();
    }

    public static TaskEvent updated(TaskResponse task) {
        return TaskEvent.builder()
                .type(TaskEventType.TASK_UPDATED)
                .payload(task)
                .boardId(task.getBoardId())
                .timestamp(Instant.now())
                .build();
    }

    public static TaskEvent deleted(UUID taskId, UUID boardId) {
        TaskResponse payload = TaskResponse.builder().id(taskId).boardId(boardId).build();
        return TaskEvent.builder()
                .type(TaskEventType.TASK_DELETED)
                .payload(payload)
                .boardId(boardId)
                .timestamp(Instant.now())
                .build();
    }

    public static TaskEvent moved(TaskResponse task, TaskStatus previousStatus, TaskStatus newStatus) {
        return TaskEvent.builder()
                .type(TaskEventType.TASK_MOVED)
                .payload(task)
                .boardId(task.getBoardId())
                .previousStatus(previousStatus)
                .newStatus(newStatus)
                .timestamp(Instant.now())
                .build();
    }
}
