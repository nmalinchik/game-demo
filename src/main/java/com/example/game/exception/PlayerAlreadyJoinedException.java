package com.example.game.exception;

public class PlayerAlreadyJoinedException extends RuntimeException {
    public PlayerAlreadyJoinedException(String message) {
        super(message);
    }
}
