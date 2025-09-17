package coffeeshout.room.domain.event;

import coffeeshout.minigame.domain.MiniGameType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class MiniGameSelectEvent {

    private String eventId;
    private RoomEventType eventType;
    private String joinCode;
    private String hostName;
    private List<MiniGameType> miniGameTypes;
    private LocalDateTime timestamp;

    public static MiniGameSelectEvent create(String joinCode, String hostName, List<MiniGameType> miniGameTypes) {
        final MiniGameSelectEvent event = new MiniGameSelectEvent();
        event.eventId = UUID.randomUUID().toString();
        event.eventType = RoomEventType.MINI_GAME_SELECT;
        event.joinCode = joinCode;
        event.hostName = hostName;
        event.miniGameTypes = miniGameTypes;
        event.timestamp = LocalDateTime.now();
        return event;
    }
}
