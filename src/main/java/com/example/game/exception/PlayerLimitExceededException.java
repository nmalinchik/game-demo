package com.example.game.exception;

public class PlayerLimitExceededException extends RuntimeException {
    public PlayerLimitExceededException(String message) {
        super(message);
    }
}
