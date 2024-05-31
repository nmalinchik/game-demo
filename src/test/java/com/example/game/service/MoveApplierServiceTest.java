package com.example.game.service;

import com.example.game.model.GameMove;
import com.example.game.model.entity.Game;
import com.example.game.model.entity.Player;
import com.example.game.model.enums.GameStatus;
import com.example.game.model.enums.PlayerSign;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MoveApplierServiceTest {

    @Mock
    private GameService gameService;

    @Mock
    private PlayerService playerService;

    @Mock
    private CuratorFramework curatorFramework;

    @Mock
    private GameValidatorService validatorService;

    @Mock
    private MoveMakerService moveMakerService;

    @InjectMocks
    private MoveApplierService moveApplierService;

    private UUID gameId;
    private Game game;
    private Player currentPlayer;
    private Player moveMaker;

    @BeforeEach
    void setUp() throws Exception {
        gameId = UUID.randomUUID();
        game = new Game();
        game.setId(gameId);
        game.setBoard(new String[][]{
                {"", "", ""},
                {"", "", ""},
                {"", "", ""}
        });
        game.setStatus(GameStatus.IN_PROGRESS);

        currentPlayer = new Player();
        currentPlayer.setId(UUID.randomUUID());
        currentPlayer.setPlayerSign(PlayerSign.X);

        moveMaker = new Player();
        moveMaker.setId(UUID.randomUUID());
        moveMaker.setPlayerSign(PlayerSign.O);

        InterProcessMutex lock = mock(InterProcessMutex.class);
        when(lock.acquire(anyLong(), any())).thenReturn(true);
        doNothing().when(lock).release();

        LockService lockService = spy(new LockService(curatorFramework));
        doReturn(lock).when(lockService).createLock(anyString());
        moveApplierService = new MoveApplierService(gameService, playerService, lockService, validatorService, moveMakerService);
    }

    @Test
    void applyMove_whenBoardChangedAndInProgress_thenUpdatesBoard() {
        String[][] newBoard = {
                {"X", "", ""},
                {"", "", ""},
                {"", "", ""}
        };
        GameMove move = createGameMove(moveMaker.getId(), newBoard);

        mockCommonDependencies();
        doNothing().when(validatorService).validateMove(any(GameMove.class));

        moveApplierService.applyMove(move);

        assertThat(game.getBoard()).isEqualTo(newBoard);

        verify(gameService).isGameOver(any(Game.class), any(Player.class));
        verify(moveMakerService).makeMove(game, currentPlayer);
    }

    @Test
    void applyMove_whenBoardNotChangedAndYourTurn_thenMakesMove() {
        String[][] newBoard = {
                {"", "", ""},
                {"", "", ""},
                {"", "", ""}
        };
        GameMove move = createGameMove(moveMaker.getId(), newBoard);

        mockCommonDependencies();
        doNothing().when(validatorService).validateMove(any(GameMove.class));

        moveApplierService.applyMove(move);
        verify(gameService, never()).isGameOver(any(Game.class), any(Player.class));
        verify(moveMakerService).makeMove(game, currentPlayer);
    }

    @Test
    void applyMove_whenGameNotInProgress_thenDoesNothing() {
        game.setStatus(GameStatus.FINISHED);
        String[][] newBoard = {
                {"X", "", ""},
                {"", "", ""},
                {"", "", ""}
        };
        GameMove move = createGameMove(moveMaker.getId(), newBoard);

        when(playerService.getCurrentPlayer()).thenReturn(currentPlayer);
        when(gameService.getGameById(gameId)).thenReturn(game);

        moveApplierService.applyMove(move);

        verify(gameService, never()).saveGame(any(Game.class));
        verify(moveMakerService, never()).makeMove(any(Game.class), any(Player.class));
    }

    private GameMove createGameMove(UUID playerId, String[][] newBoard) {
        return GameMove.builder()
                .gameId(gameId)
                .playerId(playerId)
                .newBoard(newBoard)
                .previousBoard(game.getBoard())
                .build();
    }

    private void mockCommonDependencies() {
        when(playerService.getCurrentPlayer()).thenReturn(currentPlayer);
        when(gameService.getGameById(gameId)).thenReturn(game);
        when(playerService.findPlayerById(any(UUID.class))).thenReturn(moveMaker);
    }
}
