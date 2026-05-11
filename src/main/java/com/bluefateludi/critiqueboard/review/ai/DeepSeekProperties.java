package com.bluefateludi.critiqueboard.review.ai;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "critiqueboard.ai.deepseek")
public record DeepSeekProperties(
        boolean enabled,
        String baseUrl,
        String apiKey,
        String modelName,
        int timeoutSeconds
) {

    public Duration timeout() {
        return Duration.ofSeconds(timeoutSeconds);
    }
}
