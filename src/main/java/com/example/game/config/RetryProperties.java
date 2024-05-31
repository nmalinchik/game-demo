package com.example.game.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "retry")
public class RetryProperties {
    private long initialInterval;
    private double multiplier;
    private long maxInterval;
    private int maxAttempts;
}
