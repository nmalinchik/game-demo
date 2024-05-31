package com.example.game.model;

import com.example.game.model.enums.GameStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class GameDto {
    private UUID id;
    private GameStatus status;
    private UUID lastPlayedPlayerId;
}
