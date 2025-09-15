package coffeeshout.global.redis.event.room;

import coffeeshout.room.domain.RoomState;

public record RoomStateChangedEvent(
    String joinCode,
    RoomState newState,
    String instanceId
) {}
