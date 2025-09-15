package coffeeshout.global.redis.event.player;

import coffeeshout.room.domain.menu.SelectedMenu;
import coffeeshout.room.domain.player.PlayerType;

public record PlayerJoinedEvent(
    String joinCode,
    String playerName,
    PlayerType playerType,
    SelectedMenu selectedMenu,
    boolean isReady,
    Integer colorIndex,
    String instanceId
) {}
