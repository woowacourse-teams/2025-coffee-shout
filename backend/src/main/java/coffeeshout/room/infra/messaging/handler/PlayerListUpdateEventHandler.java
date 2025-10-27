package coffeeshout.room.infra.messaging.handler;

import coffeeshout.global.redis.BaseEvent;
import coffeeshout.global.redis.EventHandler;
import coffeeshout.global.ui.WebSocketResponse;
import coffeeshout.global.websocket.LoggingSimpMessagingTemplate;
import coffeeshout.room.application.RoomService;
import coffeeshout.room.domain.event.PlayerListUpdateEvent;
import coffeeshout.room.domain.player.Player;
import coffeeshout.room.ui.response.PlayerResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PlayerListUpdateEventHandler implements EventHandler {

    private final RoomService roomService;
    private final LoggingSimpMessagingTemplate messagingTemplate;

    @Override
    public void handle(BaseEvent event) {
        final PlayerListUpdateEvent playerListUpdateEvent = (PlayerListUpdateEvent) event;
        final List<Player> players = roomService.getPlayersInternal(playerListUpdateEvent.joinCode());
        final List<PlayerResponse> responses = players.stream()
                .map(PlayerResponse::from)
                .toList();

        messagingTemplate.convertAndSend("/topic/room/" + playerListUpdateEvent.joinCode(),
                WebSocketResponse.success(responses));
    }

    @Override
    public Class<PlayerListUpdateEvent> eventType() {
        return PlayerListUpdateEvent.class;
    }
}
