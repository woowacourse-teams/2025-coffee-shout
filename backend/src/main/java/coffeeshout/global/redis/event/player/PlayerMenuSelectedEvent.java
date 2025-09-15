package coffeeshout.global.redis.event.player;

import coffeeshout.room.domain.menu.SelectedMenu;

public record PlayerMenuSelectedEvent(
    String joinCode,
    String playerName,
    SelectedMenu selectedMenu,
    String instanceId
) {}
