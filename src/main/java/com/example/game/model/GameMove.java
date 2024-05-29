package com.example.game.model;

import java.util.UUID;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class GameMove {
    private String playerId;
    private UUID gameId;
    private GameStatus gameStatus;
    private String sign;
    private String moveX;
    private String moveY;
}
