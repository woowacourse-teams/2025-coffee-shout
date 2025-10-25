package coffeeshout.global.websocket.event.session;

import coffeeshout.global.redis.BaseEvent;

public interface SessionBaseEvent extends BaseEvent {
    SessionEventType eventType();
}
