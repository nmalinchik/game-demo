package com.example.game.service;

import com.example.game.exception.EntityNotFoundException;
import com.example.game.exception.GameAlreadyExistsException;
import com.example.game.mapper.GameMapper;
import com.example.game.model.GameDto;
import com.example.game.model.GameView;
import com.example.game.model.entity.Game;
import com.example.game.model.entity.Player;
import com.example.game.model.enums.GameStatus;
import com.example.game.repos.GameRepository;
import com.example.game.service.kafka.KafkaSenderService;
import com.example.game.util.GameUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Service for managing game-related operations.
 */
@RequiredArgsConstructor
@Service
public class GameService {

    private static final String LOCK_PATH_IS_GAME_OVER = "/isGameOverLock/";

    private final GameRepository gameRepository;
    private final KafkaSenderService kafkaSenderService;
    private final LockService lockService;

    /**
     * Gets the current game.
     *
     * @return the current game as a GameDto
     */
    public GameView getCurrentGame() {
        return gameRepository.findLastCreatedGame()
                .map(GameMapper.INSTANCE::gameToGameView)
                .orElse(GameView.builder().status(GameStatus.FINISHED).build());
    }

    /**
     * Gets a game by its ID.
     *
     * @param gameId the game ID
     * @return the game entity
     * @throws EntityNotFoundException if the game is not found
     */
    public Game getGameById(UUID gameId) {
        return gameRepository.findById(gameId)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Game with id: %s not found", gameId)));
    }

    /**
     * Creates a new game.
     *
     * @return the new game as a GameDto
     */
    @Transactional
    public GameDto createNewGame() {
        if (gameRepository.findByStatus(GameStatus.IN_PROGRESS).isPresent()) {
            throw new GameAlreadyExistsException("Game already exists");
        }
        var newGame = buildNewGame();
        gameRepository.save(newGame);
        kafkaSenderService.sendGame(new GameDto(newGame.getId(), newGame.getStatus(), null));
        return GameMapper.INSTANCE.gameToGameDto(newGame);
    }

    /**
     * Saves the game entity.
     *
     * @param game the game entity to save
     */
    @Transactional
    public void saveGame(Game game) {
        gameRepository.save(game);
    }

    /**
     * Checks if the game is over.
     *
     * @param game   the game entity
     * @param player the player
     * @return true if the game is over, false otherwise
     */
    @Transactional
    public Boolean isGameOver(Game game, Player player) {
        String lockPath = LOCK_PATH_IS_GAME_OVER + game.getId();
        return lockService.executeWithLockSupplier(lockPath, () -> {
            var status = GameUtil.getCurrentGameStatus(game.getBoard());
            updateGameStatus(game, status, player);
            return !GameStatus.IN_PROGRESS.equals(status);
        });
    }

    private Game buildNewGame() {
        return Game.builder()
                .status(GameStatus.NEW)
                .board(new String[][]{{"", "", ""}, {"", "", ""}, {"", "", ""}})
                .build();
    }

    private void updateGameStatus(Game game, GameStatus status, Player player) {
        if (status.equals(GameStatus.FINISHED)) {
            game.setWinner(player);
        }
        game.setStatus(status);
        saveGame(game);
    }
}
