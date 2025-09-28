package coffeeshout.room.domain.event;

import coffeeshout.global.event.BaseEvent;
import coffeeshout.room.domain.player.Winner;
import lombok.Getter;

@Getter
public class RouletteSpinEvent extends BaseEvent implements RoomBaseEvent {
    private final RoomEventType eventType;
    private final String joinCode;
    private final String hostName;
    private final Winner winner;

    public RouletteSpinEvent(String joinCode, String hostName, Winner winner) {
        this.eventType = RoomEventType.ROULETTE_SPIN;
        this.joinCode = joinCode;
        this.hostName = hostName;
        this.winner = winner;
    }

    @Override
    public RoomEventType getEventType() {
        return eventType;
    }
}
