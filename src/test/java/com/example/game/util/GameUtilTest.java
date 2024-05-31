package com.example.game.util;

import com.example.game.model.enums.GameStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GameUtilTest {

    @Test
    void isBoardChanged_whenBoardsAreDifferent_returnsTrue() {
        String[][] board1 = {
                {"X", "", ""},
                {"", "O", ""},
                {"", "", ""}
        };
        String[][] board2 = {
                {"X", "", ""},
                {"", "X", ""},
                {"", "", ""}
        };

        assertTrue(GameUtil.isBoardChanged(board1, board2));
    }

    @Test
    void isBoardChanged_whenBoardsAreSame_returnsFalse() {
        String[][] board1 = {
                {"X", "", ""},
                {"", "O", ""},
                {"", "", ""}
        };

        assertFalse(GameUtil.isBoardChanged(board1, board1));
    }

    @Test
    void deepCopyBoard_createsExactCopy() {
        String[][] original = {
                {"X", "", ""},
                {"", "O", ""},
                {"", "", ""}
        };
        String[][] copy = GameUtil.deepCopyBoard(original);

        assertArrayEquals(original, copy);
        assertNotSame(original, copy); // Ensure it's a deep copy
    }

    @Test
    void deepCopyBoard_whenNull_returnsNull() {
        assertNull(GameUtil.deepCopyBoard(null));
    }

    @Test
    void getCurrentGameStatus_whenGameOverByRow_returnsFinished() {
        String[][] board = {
                {"X", "X", "X"},
                {"", "O", ""},
                {"", "", ""}
        };

        assertEquals(GameStatus.FINISHED, GameUtil.getCurrentGameStatus(board));
    }

    @Test
    void getCurrentGameStatus_whenGameOverByColumn_returnsFinished() {
        String[][] board = {
                {"X", "", ""},
                {"X", "O", ""},
                {"X", "", ""}
        };

        assertEquals(GameStatus.FINISHED, GameUtil.getCurrentGameStatus(board));
    }

    @Test
    void getCurrentGameStatus_whenGameOverByMainDiagonal_returnsFinished() {
        String[][] board = {
                {"X", "", ""},
                {"", "X", "O"},
                {"", "", "X"}
        };

        assertEquals(GameStatus.FINISHED, GameUtil.getCurrentGameStatus(board));
    }

    @Test
    void getCurrentGameStatus_whenGameOverByAntiDiagonal_returnsFinished() {
        String[][] board = {
                {"", "", "X"},
                {"", "X", "O"},
                {"X", "", ""}
        };

        assertEquals(GameStatus.FINISHED, GameUtil.getCurrentGameStatus(board));
    }

    @Test
    void getCurrentGameStatus_whenDraw_returnsDraw() {
        String[][] board = {
                {"X", "O", "X"},
                {"X", "X", "O"},
                {"O", "X", "O"}
        };

        assertEquals(GameStatus.DRAW, GameUtil.getCurrentGameStatus(board));
    }

    @Test
    void getCurrentGameStatus_whenInProgress_returnsInProgress() {
        String[][] board = {
                {"X", "O", ""},
                {"X", "X", "O"},
                {"O", "X", "O"}
        };

        assertEquals(GameStatus.IN_PROGRESS, GameUtil.getCurrentGameStatus(board));
    }
}
