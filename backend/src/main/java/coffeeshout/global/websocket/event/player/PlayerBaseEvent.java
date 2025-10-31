package coffeeshout.global.websocket.event.player;

import coffeeshout.global.redis.BaseEvent;
import coffeeshout.global.trace.TraceInfo;

public interface PlayerBaseEvent extends BaseEvent {

    TraceInfo traceInfo();

    PlayerEventType eventType();
}
