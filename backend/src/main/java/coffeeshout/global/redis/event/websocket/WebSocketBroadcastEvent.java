package coffeeshout.global.redis.event.websocket;

public record WebSocketBroadcastEvent(
    String destination,
    Object payload,
    String instanceId
) {}
