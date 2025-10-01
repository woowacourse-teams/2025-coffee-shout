package coffeeshout.minigame.racinggame.domain.event;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

public record TapCommandEvent(
        String eventId,
        RacingGameEventType eventType,
        LocalDateTime createdAt,
        String joinCode,
        String playerName,
        int tapCount,
        Instant timestamp
) {

    public static TapCommandEvent create(String joinCode, String playerName, int tapCount, Instant timestamp) {
        final String eventId = UUID.randomUUID().toString();
        return new TapCommandEvent(
                eventId,
                RacingGameEventType.TAP_COMMAND,
                LocalDateTime.now(),
                joinCode,
                playerName,
                tapCount,
                timestamp
        );
    }
}
