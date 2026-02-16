package com.taskboard.repository;

import com.taskboard.model.Board;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BoardRepository extends JpaRepository<Board, UUID> {

    List<Board> findByDeletedFalseOrderByCreatedAtDesc();

    Optional<Board> findByIdAndDeletedFalse(UUID id);
}
