package coffeeshout.global.config.properties;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spring.rabbitmq")
public record RabbitMQProperties(
        @NotBlank String host,
        @Min(0) @Max(65535) int port,
        @NotBlank String username,
        @NotBlank String password,
        @NotNull StompBrokerProperties stomp
) {
    public record StompBrokerProperties(
            String host,
            int port,
            String clientLogin,
            String clientPasscode,
            Heartbeat heartbeat
    ) {
    }

    public record Heartbeat(
            long send,
            long receive
    ) {
    }
}
