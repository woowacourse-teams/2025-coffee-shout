package coffeeshout.room.infra.messaging.handler;

import coffeeshout.global.redis.BaseEvent;
import coffeeshout.global.redis.EventHandler;
import coffeeshout.global.ui.WebSocketResponse;
import coffeeshout.global.websocket.LoggingSimpMessagingTemplate;
import coffeeshout.minigame.domain.MiniGameType;
import coffeeshout.room.application.RoomService;
import coffeeshout.room.domain.event.MiniGameSelectEvent;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MiniGameSelectEventHandler implements EventHandler {

    private final RoomService roomService;
    private final LoggingSimpMessagingTemplate messagingTemplate;

    @Override
    public void handle(BaseEvent event) {
        final MiniGameSelectEvent miniGameSelectEvent = (MiniGameSelectEvent) event;
        final List<MiniGameType> selectedMiniGames = roomService.updateMiniGamesInternal(
                miniGameSelectEvent.joinCode(),
                miniGameSelectEvent.hostName(),
                miniGameSelectEvent.miniGameTypes()
        );

        messagingTemplate.convertAndSend("/topic/room/" + miniGameSelectEvent.joinCode() + "/minigame",
                WebSocketResponse.success(selectedMiniGames));
    }

    @Override
    public Class<MiniGameSelectEvent> eventType() {
        return MiniGameSelectEvent.class;
    }
}
