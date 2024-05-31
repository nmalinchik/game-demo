package com.example.game.service;

import com.example.game.exception.EntityNotFoundException;
import com.example.game.exception.GameAlreadyExistsException;
import com.example.game.model.GameDto;
import com.example.game.model.GameView;
import com.example.game.model.entity.Game;
import com.example.game.model.entity.Player;
import com.example.game.model.enums.GameStatus;
import com.example.game.repos.GameRepository;
import com.example.game.service.kafka.KafkaSenderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GameServiceTest {

    @Mock
    private GameRepository gameRepository;

    @Mock
    private KafkaSenderService kafkaSenderService;

    @Mock
    private LockService lockService;

    @InjectMocks
    private GameService gameService;

    private Game game;
    private Player player;
    private UUID gameId;

    @BeforeEach
    void setUp() {
        gameId = UUID.randomUUID();
        game = Game.builder()
                .id(gameId)
                .status(GameStatus.NEW)
                .board(new String[][]{{"", "", ""}, {"", "", ""}, {"", "", ""}})
                .build();
        player = new Player();
    }

    @Test
    void getCurrentGame_returnsLastCreatedGame() {
        when(gameRepository.findLastCreatedGame()).thenReturn(Optional.of(game));
        GameView gameView = gameService.getCurrentGame();
        assertNotNull(gameView);
        assertEquals(GameStatus.NEW, gameView.getStatus());
    }

    @Test
    void getCurrentGame_returnsFinishedGame_whenNoActiveGameFound() {
        when(gameRepository.findLastCreatedGame()).thenReturn(Optional.empty());
        GameView gameView = gameService.getCurrentGame();
        assertNotNull(gameView);
        assertEquals(GameStatus.FINISHED, gameView.getStatus());
    }

    @Test
    void getGameById_returnsGame() {
        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));
        Game foundGame = gameService.getGameById(gameId);
        assertNotNull(foundGame);
        assertEquals(gameId, foundGame.getId());
    }

    @Test
    void getGameById_throwsEntityNotFoundException_whenGameNotFound() {
        when(gameRepository.findById(gameId)).thenReturn(Optional.empty());
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> gameService.getGameById(gameId));
        assertEquals(String.format("Game with id: %s not found", gameId), exception.getMessage());
    }

    @Test
    void createNewGame_createsAndSendsGame() {
        when(gameRepository.findByStatus(GameStatus.IN_PROGRESS)).thenReturn(Optional.empty());
        when(gameRepository.save(any(Game.class))).thenReturn(game);

        GameDto createdGameDto = gameService.createNewGame();

        verify(gameRepository).save(any(Game.class));
        verify(kafkaSenderService).sendGame(any(GameDto.class));
        assertNotNull(createdGameDto);
        assertEquals(GameStatus.NEW, createdGameDto.getStatus());
    }

    @Test
    void createNewGame_throwsGameAlreadyExistsException_whenInProgressGameExists() {
        when(gameRepository.findByStatus(GameStatus.IN_PROGRESS)).thenReturn(Optional.of(game));
        GameAlreadyExistsException exception = assertThrows(GameAlreadyExistsException.class, () -> gameService.createNewGame());
        assertEquals("Game already exists", exception.getMessage());
    }

    @Test
    void saveGame_savesGame() {
        gameService.saveGame(game);
        verify(gameRepository).save(game);
    }

    @Test
    void isGameOver_returnsTrue_whenGameIsOver() {
        when(lockService.executeWithLockSupplier(anyString(), any())).thenReturn(true);
        Boolean result = gameService.isGameOver(game, player);
        assertTrue(result);
    }

    @Test
    void isGameOver_returnsFalse_whenGameIsNotOver() {
        when(lockService.executeWithLockSupplier(anyString(), any())).thenReturn(false);
        Boolean result = gameService.isGameOver(game, player);
        assertFalse(result);
    }
}
