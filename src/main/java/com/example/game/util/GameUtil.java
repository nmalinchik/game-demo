package com.example.game.util;

import com.example.game.model.enums.GameStatus;
import lombok.experimental.UtilityClass;

import java.util.Arrays;

/**
 * Utility class for game-related operations.
 */
@UtilityClass
public class GameUtil {

    /**
     * Checks if the board has changed.
     *
     * @param board1 the first board state
     * @param board2 the second board state
     * @return true if the board has changed, false otherwise
     */
    public boolean isBoardChanged(String[][] board1, String[][] board2) {
        for (int i = 0; i < board1.length; i++) {
            for (int j = 0; j < board1[i].length; j++) {
                if (!board1[i][j].equals(board2[i][j])) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Creates a deep copy of the game board.
     *
     * @param original the original board state
     * @return the deep copy of the board
     */
    public String[][] deepCopyBoard(String[][] original) {
        if (original == null) {
            return null;
        }

        String[][] result = new String[original.length][];
        for (int i = 0; i < original.length; i++) {
            result[i] = Arrays.copyOf(original[i], original[i].length);
        }
        return result;
    }

    /**
     * Gets the current game status based on the board state.
     *
     * @param currentGame the current state of the game board
     * @return the current game status
     */
    public GameStatus getCurrentGameStatus(String[][] currentGame) {
        if (isGameOver(currentGame, "X") || isGameOver(currentGame, "O")) {
            return GameStatus.FINISHED;
        }

        if (!isMovesLeft(currentGame)) {
            return GameStatus.DRAW;
        }


        return GameStatus.IN_PROGRESS;
    }

    /**
     * Checks if the game is over for the given playerSign.
     *
     * @param board  the current state of the game board
     * @param playerSign the playerSign's sign
     * @return true if the game is over, false otherwise
     */
    private boolean isGameOver(String[][] board, String playerSign) {
        // Check lines
        for (int i = 0; i < 3; i++) {
            if (board[i][0].equals(playerSign) && board[i][1].equals(playerSign) && board[i][2].equals(playerSign)) {
                return true;
            }
        }

        // Check columns
        for (int j = 0; j < 3; j++) {
            if (board[0][j].equals(playerSign) && board[1][j].equals(playerSign) && board[2][j].equals(playerSign)) {
                return true;
            }
        }

        // Check diagonals
        if (board[0][0].equals(playerSign) && board[1][1].equals(playerSign) && board[2][2].equals(playerSign)) {
            return true;
        }

        return board[0][2].equals(playerSign) && board[1][1].equals(playerSign) && board[2][0].equals(playerSign);
    }

    /**
     * Checks if there are moves left on the board.
     *
     * @param board the current state of the game board
     * @return true if there are moves left, false otherwise
     */
    private boolean isMovesLeft(String[][] board) {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (board[i][j].isEmpty()) {
                    return true;
                }
            }
        }
        return false;
    }
}