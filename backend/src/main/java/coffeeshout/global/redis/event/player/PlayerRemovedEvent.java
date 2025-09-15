package coffeeshout.global.redis.event.player;

public record PlayerRemovedEvent(
    String joinCode,
    String playerName,
    String instanceId
) {}
