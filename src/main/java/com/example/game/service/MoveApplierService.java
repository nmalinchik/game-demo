package com.example.game.service;

import com.example.game.model.GameMove;
import com.example.game.model.entity.Game;
import com.example.game.model.entity.Player;
import com.example.game.model.enums.GameStatus;
import com.example.game.util.GameUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Service for handling moves in the Tic Tac Toe game.
 */
@RequiredArgsConstructor
@Service
public class MoveApplierService {

    private static final String LOCK_PATH_PREFIX = "/applyMoveLock/";

    private final GameService gameService;
    private final PlayerService playerService;
    private final LockService lockService;
    private final GameValidatorService validatorService;
    private final MoveMakerService moveMakerService;

    /**
     * Applies a move in the game.
     *
     * @param move the move to apply
     */
    public void applyMove(GameMove move) {
        String lockPath = LOCK_PATH_PREFIX + move.getGameId();
        lockService.executeWithLock(lockPath, () -> {
            validatorService.validateMove(move);
            var currentPlayer = playerService.getCurrentPlayer();
            var game = gameService.getGameById(move.getGameId());
            String[][] newBoard = move.getNewBoard();
            var isBoardChanged = GameUtil.isBoardChanged(game.getBoard(), newBoard);

            if (GameStatus.IN_PROGRESS.equals(game.getStatus())) {
                var moveMaker = playerService.findPlayerById(move.getPlayerId());
                var isYourTurn = !moveMaker.equals(currentPlayer);

                if (isBoardChanged) {
                    updateGameBoard(game, newBoard, moveMaker, currentPlayer);
                } else if (isYourTurn) {
                    moveMakerService.makeMove(game, currentPlayer);
                }
            }
        });
    }

    /**
     * Updates the game board and makes the next move if necessary.
     *
     * @param game          the game entity
     * @param newBoard      the new board state
     * @param player        the player who made the move
     * @param currentPlayer the current player
     */
    private void updateGameBoard(Game game, String[][] newBoard, Player player, Player currentPlayer) {
        game.setBoard(newBoard);
        game.setLastPlayedPlayer(player);
        if (!gameService.isGameOver(game, player) && !player.equals(currentPlayer)) {
            moveMakerService.makeMove(game, currentPlayer);
        }
    }

}
