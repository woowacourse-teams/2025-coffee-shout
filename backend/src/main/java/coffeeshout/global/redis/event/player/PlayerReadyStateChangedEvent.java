package coffeeshout.global.redis.event.player;

public record PlayerReadyStateChangedEvent(
    String joinCode,
    String playerName,
    boolean isReady,
    String instanceId
) {}
