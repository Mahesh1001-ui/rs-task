package com.taskboard.event;

import com.taskboard.dto.BoardResponse;
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
public class BoardEvent {

    private BoardEventType type;
    private BoardResponse payload;
    private UUID boardId;
    private Instant timestamp;

    public static BoardEvent created(BoardResponse board) {
        return BoardEvent.builder()
                .type(BoardEventType.BOARD_CREATED)
                .payload(board)
                .boardId(board.getId())
                .timestamp(Instant.now())
                .build();
    }

    public static BoardEvent updated(BoardResponse board) {
        return BoardEvent.builder()
                .type(BoardEventType.BOARD_UPDATED)
                .payload(board)
                .boardId(board.getId())
                .timestamp(Instant.now())
                .build();
    }

    public static BoardEvent deleted(UUID boardId) {
        return BoardEvent.builder()
                .type(BoardEventType.BOARD_DELETED)
                .boardId(boardId)
                .timestamp(Instant.now())
                .build();
    }
}
