package com.example.game.service;

import com.example.game.exception.MoveValidationException;
import com.example.game.model.GameMove;
import com.example.game.model.enums.PlayerSign;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GameValidatorServiceTest {

    private GameValidatorService gameValidatorService;

    @BeforeEach
    void setUp() {
        gameValidatorService = new GameValidatorService();
    }

    @Test
    void validateMove_whenMoveIsValid_thenNoExceptionThrown() {
        String[][] previousBoard = {
                {"X", "O", "X"},
                {"O", "", "X"},
                {"O", "X", ""}
        };
        String[][] newBoard = {
                {"X", "O", "X"},
                {"O", "X", "X"},
                {"O", "X", ""}
        };
        GameMove move = createGameMove(previousBoard, newBoard);

        assertDoesNotThrow(() -> gameValidatorService.validateMove(move));
    }

    @Test
    void validateMove_whenMoveCoordinatesAreInvalid_thenThrowsException() {
        String[][] previousBoard = {
                {"X", "O", "X"},
                {"O", "", "X"},
                {"O", "X", ""}
        };
        String[][] newBoard = {
                {"X", "O", "X"},
                {"O", "X", "X"},
                {"O", "X", ""}
        };
        GameMove move = createGameMove(previousBoard, newBoard);
        move.setMoveX(3);

        MoveValidationException exception = assertThrows(MoveValidationException.class, () -> {
            gameValidatorService.validateMove(move);
        });

        assertEquals("Invalid move coordinates.", exception.getMessage());
    }

    @Test
    void validateMove_whenPreviousBoardPositionNotEmpty_thenThrowsException() {
        String[][] previousBoard = {
                {"X", "O", "X"},
                {"O", "X", "X"},
                {"O", "X", ""}
        };
        String[][] newBoard = {
                {"X", "O", "X"},
                {"O", "X", "X"},
                {"O", "X", ""}
        };
        GameMove move = createGameMove(previousBoard, newBoard);

        MoveValidationException exception = assertThrows(MoveValidationException.class, () -> {
            gameValidatorService.validateMove(move);
        });

        assertEquals("The previous board position is not empty.", exception.getMessage());
    }

    @Test
    void validateMove_whenNewBoardPositionIncorrect_thenThrowsException() {
        String[][] previousBoard = {
                {"X", "O", "X"},
                {"O", "", "X"},
                {"O", "X", ""}
        };
        String[][] newBoard = {
                {"X", "O", "X"},
                {"O", "O", "X"},
                {"O", "X", ""}
        };
        GameMove move = createGameMove(previousBoard, newBoard);

        MoveValidationException exception = assertThrows(MoveValidationException.class, () -> {
            gameValidatorService.validateMove(move);
        });

        assertEquals("The new board position does not contain the correct sign.", exception.getMessage());
    }

    @Test
    void validateMove_whenBoardNotConsistent_thenThrowsException() {
        String[][] previousBoard = {
                {"X", "O", "X"},
                {"O", "", "X"},
                {"O", "X", ""}
        };
        String[][] newBoard = {
                {"X", "O", "X"},
                {"O", "X", "X"},
                {"X", "X", ""}
        };
        GameMove move = createGameMove(previousBoard, newBoard);

        MoveValidationException exception = assertThrows(MoveValidationException.class, () -> {
            gameValidatorService.validateMove(move);
        });

        assertEquals("The rest of the board positions must be identical.", exception.getMessage());
    }

    private GameMove createGameMove(String[][] previousBoard, String[][] newBoard) {
        return GameMove.builder()
                .sign(PlayerSign.X)
                .moveX(1)
                .moveY(1)
                .previousBoard(previousBoard)
                .newBoard(newBoard)
                .build();
    }
}