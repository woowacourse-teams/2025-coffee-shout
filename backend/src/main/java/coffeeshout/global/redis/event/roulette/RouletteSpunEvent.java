package coffeeshout.global.redis.event.roulette;

import coffeeshout.room.domain.player.Winner;

public record RouletteSpunEvent(
    String joinCode,
    Winner winner,
    String instanceId
) {}
