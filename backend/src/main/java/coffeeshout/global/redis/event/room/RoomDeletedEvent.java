package coffeeshout.global.redis.event.room;

public record RoomDeletedEvent(
    String joinCode,
    String instanceId
) {}
