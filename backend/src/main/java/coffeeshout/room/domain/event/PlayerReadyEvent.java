package coffeeshout.room.domain.event;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class PlayerReadyEvent {

    private String eventId;
    private String joinCode;
    private String playerName;
    private Boolean isReady;
    private LocalDateTime timestamp;

    public static PlayerReadyEvent create(final String joinCode, final String playerName, final Boolean isReady) {
        final PlayerReadyEvent event = new PlayerReadyEvent();
        event.eventId = UUID.randomUUID().toString();
        event.joinCode = joinCode;
        event.playerName = playerName;
        event.isReady = isReady;
        event.timestamp = LocalDateTime.now();
        return event;
    }
}
