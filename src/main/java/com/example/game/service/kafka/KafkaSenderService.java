package com.example.game.service.kafka;

import com.example.game.model.GameDto;
import com.example.game.model.GameMove;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaSenderService {

    @Value(value = "${game.topic.game.name}")
    private String gameTopicName;

    @Value(value = "${game.topic.movies.name}")
    private String gameMoveTopicName;

    private final KafkaTemplate<String, Object> gameMoveKafkaTemplate;
    private final KafkaTemplate<String, Object> gameDtoKafkaTemplate;
    private final RetryTemplate retryTemplate;

    /**
     * Send a GameDto to the game topic.
     *
     * @param gameDto the game data transfer object
     */
    public void sendGame(GameDto gameDto) {
        retryTemplate.execute(context -> {
            CompletableFuture<SendResult<String, Object>> future = gameDtoKafkaTemplate.send(gameTopicName, gameDto);
            future.whenComplete(handleSendResult(gameDto));
            return null;
        });
    }

    /**
     * Send a GameMove to the game moves topic.
     *
     * @param gameMove the game move
     */
    public void sendGameMove(GameMove gameMove) {
        retryTemplate.execute(context -> {
            CompletableFuture<SendResult<String, Object>> future = gameMoveKafkaTemplate.send(gameMoveTopicName, gameMove);
            future.whenComplete(handleSendResult(gameMove));
            return null;
        });
    }

    private <T> BiConsumer<SendResult<String, Object>, Throwable> handleSendResult(T message) {
        return (result, ex) -> {
            if (ex == null) {
                log.info("Sent [{}] with offset=[{}]", message, result.getRecordMetadata().offset());
            } else {
                log.error("Unable to send [{}] due to: {}", message, ex.getMessage());
                throw new RuntimeException(ex);
            }
        };
    }
}