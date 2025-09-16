package coffeeshout.global.redis.event.minigame;

import coffeeshout.minigame.domain.MiniGameType;
import java.util.List;

public record MiniGamesUpdatedEvent(
    String joinCode,
    List<MiniGameType> miniGameTypes,
    String instanceId
) {}
