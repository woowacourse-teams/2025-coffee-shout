package coffeeshout.room.application;

import coffeeshout.room.domain.JoinCode;
import coffeeshout.room.domain.Room;
import coffeeshout.room.domain.event.PlayerKickEvent;
import coffeeshout.room.domain.event.RoomEventPublisher;
import coffeeshout.room.domain.player.PlayerName;
import coffeeshout.room.domain.service.PlayerCommandService;
import coffeeshout.room.domain.service.RoomQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoomPlayerService {

    private final RoomQueryService roomQueryService;
    private final PlayerCommandService playerCommandService;
    private final RoomEventPublisher roomEventPublisher;

    public boolean isGuestNameDuplicated(String joinCode, String guestName) {
        return playerCommandService.isGuestNameDuplicated(joinCode, guestName);
    }

    public boolean kickPlayer(String joinCode, String playerName) {
        final boolean exists = hasPlayer(joinCode, playerName);

        if (exists) {
            final PlayerKickEvent event = new PlayerKickEvent(joinCode, playerName);
            roomEventPublisher.publish(event);
        }

        return exists;
    }

    private boolean hasPlayer(String joinCode, String playerName) {
        final Room room = roomQueryService.getByJoinCode(new JoinCode(joinCode));
        return room.hasPlayer(new PlayerName(playerName));
    }
}
