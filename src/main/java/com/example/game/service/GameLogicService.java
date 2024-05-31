package com.example.game.service;

import com.example.game.model.enums.PlayerSign;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Service for managing Tic-Tac-Toe game logic, including finding the best move using minimax algorithm.
 */
@Service
public class GameLogicService {

    private final Random random = new Random();
    private static final int WIN_SCORE = 10;
    private static final int LOSS_SCORE = -10;
    private static final int DRAW_SCORE = 0;
    private static final int BOARD_SIZE = 3;
    private static final int MAX_DEPTH = 0;

    /**
     * Finds the best move for the given player sign using the minimax algorithm.
     *
     * @param board      the current state of the game board
     * @param playerSign the sign of the player ('X' or 'O')
     * @return the best move as an array with two elements: row and column
     */
    public int[] findBestMove(String[][] board, PlayerSign playerSign) {
        int bestVal = Integer.MIN_VALUE;
        List<int[]> bestMoves = new ArrayList<>();

        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (board[i][j].isEmpty()) {
                    board[i][j] = playerSign.name();
                    int moveVal = minimax(board, MAX_DEPTH, false, playerSign);
                    board[i][j] = "";

                    if (moveVal > bestVal) {
                        bestVal = moveVal;
                        bestMoves.clear();
                        bestMoves.add(new int[]{i, j});
                    } else if (moveVal == bestVal) {
                        bestMoves.add(new int[]{i, j});
                    }
                }
            }
        }
        return bestMoves.get(random.nextInt(bestMoves.size()));
    }

    public int[] findRandomMove(String[][] board) {
        List<int[]> availableMoves = new ArrayList<>();
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (board[i][j].isEmpty()) {
                    availableMoves.add(new int[]{i, j});
                }
            }
        }
        return availableMoves.get(random.nextInt(availableMoves.size()));
    }

    private int minimax(String[][] board, int depth, boolean isMax, PlayerSign playerSign) {
        int score = evaluate(board, playerSign);

        if (score == WIN_SCORE) return score - depth;
        if (score == LOSS_SCORE) return score + depth;
        if (!isMovesLeft(board)) return DRAW_SCORE;

        if (isMax) {
            int best = Integer.MIN_VALUE;
            for (int i = 0; i < BOARD_SIZE; i++) {
                for (int j = 0; j < BOARD_SIZE; j++) {
                    if (board[i][j].isEmpty()) {
                        board[i][j] = playerSign.name();
                        best = Math.max(best, minimax(board, depth + 1, false, playerSign));
                        board[i][j] = "";
                    }
                }
            }
            return best;
        } else {
            int best = Integer.MAX_VALUE;
            PlayerSign opponentSign = getOpponentSign(playerSign);
            for (int i = 0; i < BOARD_SIZE; i++) {
                for (int j = 0; j < BOARD_SIZE; j++) {
                    if (board[i][j].isEmpty()) {
                        board[i][j] = opponentSign.name();
                        best = Math.min(best, minimax(board, depth + 1, true, playerSign));
                        board[i][j] = "";
                    }
                }
            }
            return best;
        }
    }

    private boolean isMovesLeft(String[][] board) {
        for (int i = 0; i < BOARD_SIZE; i++)
            for (int j = 0; j < BOARD_SIZE; j++)
                if (board[i][j].isEmpty())
                    return true;
        return false;
    }

    private int evaluate(String[][] board, PlayerSign playerSign) {
        for (int row = 0; row < BOARD_SIZE; row++) {
            if (board[row][0].equals(board[row][1]) && board[row][1].equals(board[row][2])) {
                if (board[row][0].equals(playerSign.name()))
                    return WIN_SCORE;
                else if (board[row][0].equals(getOpponentSign(playerSign).name()))
                    return LOSS_SCORE;
            }
        }

        for (int col = 0; col < BOARD_SIZE; col++) {
            if (board[0][col].equals(board[1][col]) && board[1][col].equals(board[2][col])) {
                if (board[0][col].equals(playerSign.name()))
                    return WIN_SCORE;
                else if (board[0][col].equals(getOpponentSign(playerSign).name()))
                    return LOSS_SCORE;
            }
        }

        if (board[0][0].equals(board[1][1]) && board[1][1].equals(board[2][2])) {
            if (board[0][0].equals(playerSign.name()))
                return WIN_SCORE;
            else if (board[0][0].equals(getOpponentSign(playerSign).name()))
                return LOSS_SCORE;
        }

        if (board[0][2].equals(board[1][1]) && board[1][1].equals(board[2][0])) {
            if (board[0][2].equals(playerSign.name()))
                return WIN_SCORE;
            else if (board[0][2].equals(getOpponentSign(playerSign).name()))
                return LOSS_SCORE;
        }

        return DRAW_SCORE;
    }

    private PlayerSign getOpponentSign(PlayerSign playerSign) {
        return (playerSign == PlayerSign.X) ? PlayerSign.O : PlayerSign.X;
    }
}
