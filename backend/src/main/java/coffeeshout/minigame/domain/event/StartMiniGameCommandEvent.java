package coffeeshout.minigame.domain.event;

import coffeeshout.global.event.BaseEvent;
import java.time.LocalDateTime;
import lombok.Getter;

@Getter
public class StartMiniGameCommandEvent extends BaseEvent implements MiniGameBaseEvent {
    private final MiniGameEventType eventType;
    private final String joinCode;
    private final String hostName;

    public StartMiniGameCommandEvent(String joinCode, String hostName) {
        this.eventType = MiniGameEventType.START_MINIGAME_COMMAND;
        this.joinCode = joinCode;
        this.hostName = hostName;
    }

    @Override
    public MiniGameEventType getEventType() {
        return eventType;
    }

    @Override
    public LocalDateTime getCreatedAt() {
        return getTimestamp();
    }
}
