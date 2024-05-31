package com.example.game.repos;

import com.example.game.model.enums.GameStatus;
import com.example.game.model.entity.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface GameRepository extends JpaRepository<Game, UUID> {

    Optional<Game> findByStatus(GameStatus status);

    @Query("SELECT game FROM Game game ORDER BY game.createdAt DESC limit 1")
    Optional<Game> findLastCreatedGame();
}