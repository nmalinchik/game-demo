package com.example.game.service.kafka;

import com.example.game.model.GameDto;
import com.example.game.model.GameMove;
import com.example.game.service.MoveApplierService;
import com.example.game.service.MoveMakerService;
import com.example.game.service.PlayerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaListenerService {

    private final PlayerService playerService;
    private final MoveApplierService moveApplierService;
    private final MoveMakerService moveMakerService;

    /**
     * Listener for game topic.
     *
     * @param gameDto the game data transfer object
     */
    @KafkaListener(topics = "${game.topic.game.name}", groupId = "${game.topic.game.group-id}")
    public void listenGameTopic(GameDto gameDto) {
        if (gameDto == null) {
            log.warn("Received null gameDto");
            return;
        }
        log.info("Received gameDto: {}", gameDto);

        switch (gameDto.getStatus()) {
            case NEW -> playerService.joinGame(gameDto);
            case IN_PROGRESS -> moveMakerService.makeMove(gameDto);
            default -> log.warn("Dont have any logic for messages with this game status: {}", gameDto.getStatus());
        }
    }

    /**
     * Listener for game moves topic.
     *
     * @param gameMove the game move
     */
    @KafkaListener(topics = "${game.topic.movies.name}", groupId = "${game.topic.movies.group-id}")
    public void listenGameMoviesTopic(GameMove gameMove) {
        if (gameMove == null) {
            log.warn("Received null gameMove");
            return;
        }
        log.info("Received gameMove: {}", gameMove);

        moveApplierService.applyMove(gameMove);
    }

}
