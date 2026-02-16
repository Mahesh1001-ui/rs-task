package com.taskboard.repository;

import com.taskboard.model.Task;
import com.taskboard.model.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {

    List<Task> findByBoardIdOrderByCreatedAtDesc(UUID boardId);

    List<Task> findByBoardIdAndStatusOrderByCreatedAtDesc(UUID boardId, TaskStatus status);

    @Query("SELECT t FROM Task t WHERE t.board.id = :boardId AND " +
           "(LOWER(t.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(t.description) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<Task> searchByBoardIdAndQuery(@Param("boardId") UUID boardId, @Param("query") String query);

    void deleteByBoardId(UUID boardId);
}
