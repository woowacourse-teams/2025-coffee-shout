package coffeeshout.minigame.domain.event;

import coffeeshout.global.event.BaseEvent;
import java.time.LocalDateTime;
import lombok.Getter;

@Getter
public class SelectCardCommandEvent extends BaseEvent implements MiniGameBaseEvent {
    private final MiniGameEventType eventType;
    private final String joinCode;
    private final String playerName;
    private final Integer cardIndex;

    public SelectCardCommandEvent(String joinCode, String playerName, Integer cardIndex) {
        this.eventType = MiniGameEventType.SELECT_CARD_COMMAND;
        this.joinCode = joinCode;
        this.playerName = playerName;
        this.cardIndex = cardIndex;
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
