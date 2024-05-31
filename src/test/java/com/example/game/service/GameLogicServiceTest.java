package com.example.game.service;


import com.example.game.model.enums.PlayerSign;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameLogicServiceTest {
    private GameLogicService gameLogicService;

    @BeforeEach
    void setUp() {
        gameLogicService = new GameLogicService();
    }

    @Test
    void findBestMove_whenWinningMoveAvailable_thenReturnsWinningMove() {
        String[][] board = {
                {"X", "O", "X"},
                {"", "O", ""},
                {"X", "", ""}
        };
        int[] bestMove = gameLogicService.findBestMove(board, PlayerSign.O);
        assertNotNull(bestMove);
        assertArrayEquals(new int[]{2, 1}, bestMove);
    }

    @Test
    void findBestMove_whenBlockingMoveAvailable_thenReturnsBlockingMove() {
        String[][] board = {
                {"X", "X", ""},
                {"", "O", ""},
                {"O", "", ""}
        };
        int[] bestMove = gameLogicService.findBestMove(board, PlayerSign.O);
        assertNotNull(bestMove);
        assertArrayEquals(new int[]{0, 2}, bestMove);
    }

    @Test
    void findBestMove_whenBoardIsEmpty_thenReturnsAnyValidMove() {
        String[][] board = {
                {"", "", ""},
                {"", "", ""},
                {"", "", ""}
        };
        int[] bestMove = gameLogicService.findBestMove(board, PlayerSign.X);
        assertNotNull(bestMove);
        assertEquals(2, bestMove.length);
    }

    @Test
    void findRandomMove_whenCalled_thenReturnsRandomMove() {
        String[][] board = {
                {"X", "O", "X"},
                {"X", "O", "O"},
                {"", "X", ""}
        };
        int[] randomMove = gameLogicService.findRandomMove(board);
        assertNotNull(randomMove);
        assertTrue((randomMove[0] == 2 && randomMove[1] == 0) || (randomMove[0] == 2 && randomMove[1] == 2));
    }

    @Test
    void findBestMove_whenOnlyOneMoveLeft_thenReturnsLastMove() {
        String[][] board = {
                {"X", "O", "X"},
                {"X", "O", "O"},
                {"O", "X", ""}
        };
        int[] bestMove = gameLogicService.findBestMove(board, PlayerSign.X);
        assertNotNull(bestMove);
        assertArrayEquals(new int[]{2, 2}, bestMove);
    }

    @Test
    void findBestMove_whenNoMovesLeft_thenThrowsException() {
        String[][] board = {
                {"X", "O", "X"},
                {"X", "O", "O"},
                {"O", "X", "X"}
        };
        assertThrows(IllegalArgumentException.class, () -> {
            gameLogicService.findBestMove(board, PlayerSign.X);
        });
    }
}