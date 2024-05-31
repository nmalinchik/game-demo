package com.example.game.service;

import com.example.game.exception.MoveValidationException;
import com.example.game.model.GameMove;
import org.springframework.stereotype.Service;

@Service
public class GameValidatorService {

    /**
     * Validates the move.
     *
     * @param move the move to validate
     * @throws MoveValidationException if the move is invalid
     */
    public void validateMove(GameMove move) throws MoveValidationException {
        var moveX = move.getMoveX();
        var moveY = move.getMoveY();
        var previousBoard = move.getPreviousBoard();
        var newBoard = move.getNewBoard();
        if (moveX == null || moveY == null || moveX < 0 || moveX > 2 || moveY < 0 || moveY > 2) {
            throw new MoveValidationException("Invalid move coordinates.");
        }

        if (!previousBoard[moveX][moveY].isEmpty()) {
            throw new MoveValidationException("The previous board position is not empty.");
        }

        if (!newBoard[moveX][moveY].equals(move.getSign().name())) {
            throw new MoveValidationException("The new board position does not contain the correct sign.");
        }

        validateBoardConsistency(previousBoard, newBoard, moveX, moveY);
    }

    private void validateBoardConsistency(String[][] previousBoard, String[][] newBoard, int moveX, int moveY) {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (i == moveX && j == moveY) {
                    continue;
                }
                if (!previousBoard[i][j].equals(newBoard[i][j])) {
                    throw new MoveValidationException("The rest of the board positions must be identical.");
                }
            }
        }
    }
}
