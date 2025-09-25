package coffeeshout.global.websocket.event.player;

import java.time.LocalDateTime;

public interface PlayerBaseEvent {
    String getEventId();
    
    PlayerEventType getEventType();
    
    LocalDateTime getTimestamp();
}
