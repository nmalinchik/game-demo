package com.example.game.service;

import java.util.concurrent.CompletableFuture;

import com.example.game.model.GameMove;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class MoviesTopicService {

    @Value(value = "${game.topic.name}")
    private String topicName;

    private final KafkaTemplate<String, GameMove> kafkaTemplate;

    public void sendMessage(GameMove gameMove) {
        CompletableFuture<SendResult<String, GameMove>> future = kafkaTemplate.send(topicName, gameMove);
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                System.out.println("Sent gameMove=[" + gameMove +
                                           "] with offset=[" + result.getRecordMetadata().offset() + "]");
            } else {
                System.out.println("Unable to send gameMove=[" +
                                           gameMove + "] due to : " + ex.getMessage());
            }
        });
    }

    @KafkaListener(topics = "${game.topic.name}", groupId = "${game.topic.group-id}")
    public void listenGroupFoo(GameMove gameMove) {
        System.out.println("Received Message in group foo: " + gameMove);
    }

}
