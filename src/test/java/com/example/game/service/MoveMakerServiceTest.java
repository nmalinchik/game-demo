package com.example.game.service;

import com.example.game.model.GameDto;
import com.example.game.model.GameMove;
import com.example.game.model.entity.Game;
import com.example.game.model.entity.Player;
import com.example.game.model.enums.PlayerSign;
import com.example.game.service.kafka.KafkaSenderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Random;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MoveMakerServiceTest {

    @Mock
    private PlayerService playerService;

    @Mock
    private GameLogicService gameLogicService;

    @Mock
    private KafkaSenderService kafkaSenderService;

    @Mock
    private GameService gameService;

    @Mock
    private Random random;

    @InjectMocks
    private MoveMakerService moveMakerService;

    private UUID gameId;
    private Game game;
    private Player player;

    @BeforeEach
    void setUp() {
        gameId = UUID.randomUUID();
        game = new Game();
        game.setId(gameId);
        game.setBoard(new String[][]{
                {"", "", ""},
                {"", "", ""},
                {"", "", ""}
        });
        player = new Player();
        player.setId(UUID.randomUUID());
        player.setPlayerSign(PlayerSign.X);
    }

    @Test
    void makeMove_whenCurrentPlayerTurn_thenMakesMove() {
        GameDto gameDto = createGameDto(UUID.randomUUID());
        mockCommonDependencies();
        when(gameLogicService.findBestMove(any(String[][].class), eq(PlayerSign.X))).thenReturn(new int[]{0, 0});
        when(random.nextDouble()).thenReturn(0.2);

        moveMakerService.makeMove(gameDto);

        verify(kafkaSenderService).sendGameMove(any(GameMove.class));
    }

    @Test
    void makeMove_whenNotCurrentPlayerTurn_thenDoesNotMakeMove() {
        UUID playerId = UUID.randomUUID();
        GameDto gameDto = createGameDto(playerId);
        player.setId(playerId);

        when(playerService.getCurrentPlayer()).thenReturn(player);

        moveMakerService.makeMove(gameDto);

        verify(kafkaSenderService, never()).sendGameMove(any(GameMove.class));
    }

    @Test
    void makeMove_whenWrongMoveProbability_thenMakesRandomMove() {
        GameDto gameDto = createGameDto(UUID.randomUUID());
        mockCommonDependencies();
        when(gameLogicService.findRandomMove(any(String[][].class))).thenReturn(new int[]{0, 0});
        when(random.nextDouble()).thenReturn(0.05);

        moveMakerService.makeMove(gameDto);

        verify(kafkaSenderService).sendGameMove(any(GameMove.class));
    }

    @Test
    void makeMove_whenBestMove_thenMakesBestMove() {
        GameDto gameDto = createGameDto(UUID.randomUUID());
        mockCommonDependencies();
        when(gameLogicService.findBestMove(any(String[][].class), eq(PlayerSign.X))).thenReturn(new int[]{0, 0});
        when(random.nextDouble()).thenReturn(0.2);

        moveMakerService.makeMove(gameDto);

        verify(kafkaSenderService).sendGameMove(any(GameMove.class));
    }

    private GameDto createGameDto(UUID lastPlayedPlayerId) {
        return GameDto.builder().id(gameId).lastPlayedPlayerId(lastPlayedPlayerId).build();
    }

    private void mockCommonDependencies() {
        when(playerService.getCurrentPlayer()).thenReturn(player);
        when(gameService.getGameById(gameId)).thenReturn(game);
    }
}
