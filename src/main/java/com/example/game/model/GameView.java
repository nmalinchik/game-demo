package com.example.game.model;

import com.example.game.model.enums.GameStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class GameView {
    private GameStatus status;
    private String winner;
    private String[][] board;
}
