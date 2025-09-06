package coffeeshout.config.properties;

import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "room.qr")
public record QrProperties(String prefix, @Positive int height, @Positive int width, PresignedUrl presignedUrl) {

    public record PresignedUrl(@Positive int expirationHours) {
    }
}
