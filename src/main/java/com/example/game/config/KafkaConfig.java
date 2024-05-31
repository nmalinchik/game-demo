package com.example.game.config;

import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    @Value(value = "${spring.kafka.bootstrap-servers}")
    private String bootstrapAddress;

    @Value(value = "${game.topic.game.name}")
    private String gameTopicName;

    @Value(value = "${game.topic.movies.name}")
    private String gameMoviesTopicName;

    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        return new KafkaAdmin(configs);
    }

    @Bean
    public NewTopic gameTopic() {
        return new NewTopic(gameTopicName, 1, (short) 1);
    }

    @Bean
    public NewTopic gameMoviesTopic() {
        return new NewTopic(gameMoviesTopicName, 1, (short) 1);
    }

}
