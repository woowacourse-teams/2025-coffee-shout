package coffeeshout.room.application.handler;

import coffeeshout.global.ui.WebSocketResponse;
import coffeeshout.global.websocket.LoggingSimpMessagingTemplate;
import coffeeshout.minigame.domain.MiniGameType;
import coffeeshout.room.application.RoomEventHandler;
import coffeeshout.room.domain.JoinCode;
import coffeeshout.room.domain.Playable;
import coffeeshout.room.domain.Room;
import coffeeshout.room.domain.event.MiniGameSelectEvent;
import coffeeshout.room.domain.event.RoomEventType;
import coffeeshout.room.domain.player.PlayerName;
import coffeeshout.room.domain.service.RoomCommandService;
import coffeeshout.room.domain.service.RoomQueryService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MiniGameSelectEventHandler implements RoomEventHandler<MiniGameSelectEvent> {

    private final RoomCommandService roomCommandService;
    private final RoomQueryService roomQueryService;
    private final LoggingSimpMessagingTemplate messagingTemplate;

    @Override
    public void handle(MiniGameSelectEvent event) {
        try {
            log.info("미니게임 선택 이벤트 수신: eventId={}, joinCode={}, hostName={}, miniGameTypes={}",
                    event.eventId(), event.joinCode(), event.hostName(), event.miniGameTypes());

            final Room room = roomQueryService.getByJoinCode(new JoinCode(event.joinCode()));
            room.clearMiniGames();

            event.miniGameTypes().forEach(miniGameType -> {
                final Playable miniGame = miniGameType.createMiniGame(event.joinCode());
                room.addMiniGame(new PlayerName(event.hostName()), miniGame);
            });

            roomCommandService.save(room);

            final List<MiniGameType> selectedMiniGames = room.getAllMiniGame().stream()
                    .map(Playable::getMiniGameType)
                    .toList();

            messagingTemplate.convertAndSend("/topic/room/" + event.joinCode() + "/minigame",
                    WebSocketResponse.success(selectedMiniGames));

            log.info("미니게임 선택 이벤트 처리 완료: eventId={}, joinCode={}, selectedCount={}",
                    event.eventId(), event.joinCode(), selectedMiniGames.size());

        } catch (Exception e) {
            log.error("미니게임 선택 이벤트 처리 실패", e);
        }
    }

    @Override
    public RoomEventType getSupportedEventType() {
        return RoomEventType.MINI_GAME_SELECT;
    }
}
