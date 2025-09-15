package coffeeshout.global.redis.event.minigame;

import coffeeshout.minigame.domain.MiniGameType;

public record MiniGameRoundProgressEvent(
    String joinCode,
    MiniGameType miniGameType,
    Object gameState,
    String instanceId
) {}
