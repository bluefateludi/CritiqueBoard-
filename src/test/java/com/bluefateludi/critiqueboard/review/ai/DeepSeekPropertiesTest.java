package com.bluefateludi.critiqueboard.review.ai;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertySources;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class DeepSeekPropertiesTest {

    @Test
    void bindsDeepSeekConfiguration() {
        StandardEnvironment environment = new StandardEnvironment();
        environment.getPropertySources().addFirst(new MapPropertySource("test", Map.of(
                "critiqueboard.ai.deepseek.enabled", "true",
                "critiqueboard.ai.deepseek.base-url", "https://api.deepseek.com",
                "critiqueboard.ai.deepseek.api-key", "test-key",
                "critiqueboard.ai.deepseek.model-name", "deepseek-v4-flash",
                "critiqueboard.ai.deepseek.timeout-seconds", "45",
                "critiqueboard.ai.deepseek.input-cost-per-million-tokens", "0.14",
                "critiqueboard.ai.deepseek.output-cost-per-million-tokens", "0.28"
        )));

        DeepSeekProperties properties = new Binder(ConfigurationPropertySources.get(environment))
                .bind("critiqueboard.ai.deepseek", Bindable.of(DeepSeekProperties.class))
                .orElseThrow(IllegalStateException::new);

        assertThat(properties.enabled()).isTrue();
        assertThat(properties.baseUrl()).isEqualTo("https://api.deepseek.com");
        assertThat(properties.apiKey()).isEqualTo("test-key");
        assertThat(properties.modelName()).isEqualTo("deepseek-v4-flash");
        assertThat(properties.timeout()).isEqualTo(Duration.ofSeconds(45));
        assertThat(properties.inputCostPerMillionTokens()).isEqualByComparingTo(new BigDecimal("0.14"));
        assertThat(properties.outputCostPerMillionTokens()).isEqualByComparingTo(new BigDecimal("0.28"));
    }
}
