package com.example.game.model;

import com.example.game.model.enums.PlayerSign;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameMove {
    private UUID playerId;
    private UUID gameId;
    private PlayerSign sign;
    private Integer moveX;
    private Integer moveY;
    private String[][] previousBoard;
    private String[][] newBoard;
}
