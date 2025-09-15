package coffeeshout.global.redis.event.minigame;

public record CardSelectedEvent(
    String joinCode,
    String playerName,
    Integer cardIndex,
    String instanceId
) {}
