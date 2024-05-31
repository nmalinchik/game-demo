package com.example.game.controller;

import com.example.game.model.GameDto;
import com.example.game.model.GameView;
import com.example.game.service.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
@RestController
@RequestMapping("/game")
public class GameController {

    private final GameService gameService;

    @PostMapping("/new")
    public ResponseEntity<GameDto> startNewGame() {
        return ResponseEntity.ok(gameService.createNewGame());
    }

    @GetMapping
    public ResponseEntity<GameView> getCurrentGame() {
        return ResponseEntity.ok(gameService.getCurrentGame());
    }
}
