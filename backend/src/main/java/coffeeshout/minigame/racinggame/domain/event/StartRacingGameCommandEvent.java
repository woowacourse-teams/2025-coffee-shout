package coffeeshout.minigame.racinggame.domain.event;

import java.time.LocalDateTime;
import java.util.UUID;

public record StartRacingGameCommandEvent(
        String eventId,
        RacingGameEventType eventType,
        LocalDateTime createdAt,
        String joinCode,
        String hostName
) {

    public static StartRacingGameCommandEvent create(String joinCode, String hostName) {
        final String eventId = UUID.randomUUID().toString();
        return new StartRacingGameCommandEvent(
                eventId,
                RacingGameEventType.START_RACING_GAME_COMMAND,
                LocalDateTime.now(),
                joinCode,
                hostName
        );
    }
}
