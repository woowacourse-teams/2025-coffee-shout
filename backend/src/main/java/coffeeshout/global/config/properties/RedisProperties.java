package coffeeshout.global.config.properties;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "spring.data.redis")
public record RedisProperties(
        @NotBlank String host,
        @Positive int port,
        Ssl ssl
) {
    public record Ssl(boolean enabled) {
    }
}