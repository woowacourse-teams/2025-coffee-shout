package coffeeshout.global.redis.event.room;

import coffeeshout.room.domain.menu.SelectedMenu;

public record RoomCreatedEvent(
    String joinCode,
    String hostName,
    SelectedMenu hostMenu,
    String qrCodeUrl,
    String instanceId
) {}
