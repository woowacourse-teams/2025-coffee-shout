package coffeeshout.global.redis.event.minigame;

import coffeeshout.minigame.domain.MiniGameResult;
import coffeeshout.minigame.domain.MiniGameType;

public record MiniGameCompletedEvent(
    String joinCode,
    MiniGameType miniGameType,
    MiniGameResult result,
    String instanceId
) {}
