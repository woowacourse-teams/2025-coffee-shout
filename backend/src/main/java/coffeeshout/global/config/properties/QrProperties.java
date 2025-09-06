package coffeeshout.global.config.properties;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "room.qr")
public record QrProperties(String prefix, @Positive int height, @Positive int width, @NotNull PresignedUrl presignedUrl) {

    public record PresignedUrl(@Positive @Max(168) int expirationHours) {
    }
}
