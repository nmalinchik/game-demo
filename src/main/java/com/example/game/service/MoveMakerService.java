package com.example.game.service;

import com.example.game.model.GameDto;
import com.example.game.model.GameMove;
import com.example.game.model.entity.Game;
import com.example.game.model.entity.Player;
import com.example.game.service.kafka.KafkaSenderService;
import com.example.game.util.GameUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Random;

/**
 * Service for making moves in the Tic Tac Toe game.
 */
@Service
@RequiredArgsConstructor
public class MoveMakerService {

    public static final int SLEEP_IN_MILLISECONDS = 1000;
    private static final Double WRONG_MOVE_PROBABILITY = 0.1;
    private final Random random;

    private final PlayerService playerService;
    private final GameLogicService gameLogicService;
    private final KafkaSenderService kafkaSenderService;
    private final GameService gameService;

    /**
     * Makes a move in the game based on the game DTO.
     *
     * @param gameDto the game data transfer object
     */
    public void makeMove(GameDto gameDto) {
        var currentPlayer = playerService.getCurrentPlayer();
        var game = gameService.getGameById(gameDto.getId());

        if (isCurrentPlayerTurn(gameDto, currentPlayer)) {
            makeMove(game, currentPlayer);
        }
    }

    /**
     * Makes a move in the game for the current player.
     *
     * @param game          the game entity
     * @param currentPlayer the current player
     */
    public void makeMove(Game game, Player currentPlayer) {
        String[][] newBoard = GameUtil.deepCopyBoard(game.getBoard());
        var randomValue = random.nextDouble();
        int[] nextMove = randomValue < WRONG_MOVE_PROBABILITY ?
                gameLogicService.findRandomMove(newBoard) :
                gameLogicService.findBestMove(newBoard, currentPlayer.getPlayerSign());
        applyMoveToBoard(currentPlayer, newBoard, nextMove);

        var gameMove = buildGameMove(game, currentPlayer, nextMove, newBoard);
        sleepBeforeMoving();
        kafkaSenderService.sendGameMove(gameMove);
    }

    private boolean isCurrentPlayerTurn(GameDto gameDto, Player currentPlayer) {
        return Objects.nonNull(gameDto.getLastPlayedPlayerId()) &&
                !currentPlayer.getId().equals(gameDto.getLastPlayedPlayerId());
    }

    private void applyMoveToBoard(Player currentPlayer, String[][] newBoard, int[] nextMove) {
        newBoard[nextMove[0]][nextMove[1]] = currentPlayer.getPlayerSign().name();
    }

    private static GameMove buildGameMove(Game game, Player currentPlayer, int[] nextMove, String[][] newBoard) {
        return GameMove.builder()
                .playerId(currentPlayer.getId())
                .gameId(game.getId())
                .sign(currentPlayer.getPlayerSign())
                .moveX(nextMove[0])
                .moveY(nextMove[1])
                .previousBoard(game.getBoard())
                .newBoard(newBoard)
                .build();
    }

    /**
     * Sleeps for a delay to simulate move processing time.
     */
    void sleepBeforeMoving() {
        try {
            Thread.sleep(SLEEP_IN_MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Thread was interrupted, Failed to complete operation");
        }
    }
}
