package coffeeshout.global.redis.event.minigame;

import coffeeshout.minigame.domain.MiniGameType;
import java.util.List;

public record MiniGameStartedEvent(
    String joinCode,
    MiniGameType miniGameType,
    List<String> playerNames,
    String instanceId
) {}
