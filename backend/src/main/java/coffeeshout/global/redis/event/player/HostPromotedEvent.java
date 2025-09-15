package coffeeshout.global.redis.event.player;

public record HostPromotedEvent(
    String joinCode,
    String newHostName,
    String instanceId
) {}
