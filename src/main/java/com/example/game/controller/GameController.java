package com.example.game.controller;

import com.example.game.model.GameMove;
import com.example.game.service.MoviesTopicService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/game")
public class GameController {

    private final MoviesTopicService moviesTopicService;

    @PostMapping
    public void sendMove(@RequestBody GameMove move) {
        moviesTopicService.sendMessage(move);
    }

}
