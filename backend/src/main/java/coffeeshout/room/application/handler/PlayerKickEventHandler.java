package coffeeshout.room.application.handler;

import coffeeshout.global.ui.WebSocketResponse;
import coffeeshout.global.websocket.LoggingSimpMessagingTemplate;
import coffeeshout.room.application.RoomEventHandler;
import coffeeshout.room.domain.JoinCode;
import coffeeshout.room.domain.Room;
import coffeeshout.room.domain.event.PlayerKickEvent;
import coffeeshout.room.domain.event.RoomEventType;
import coffeeshout.room.domain.player.Player;
import coffeeshout.room.domain.player.PlayerName;
import coffeeshout.room.domain.service.RoomCommandService;
import coffeeshout.room.domain.service.RoomQueryService;
import coffeeshout.room.ui.response.PlayerResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PlayerKickEventHandler implements RoomEventHandler<PlayerKickEvent> {

    private final RoomCommandService roomCommandService;
    private final RoomQueryService roomQueryService;
    private final LoggingSimpMessagingTemplate messagingTemplate;

    @Override
    public void handle(PlayerKickEvent event) {
        try {
            log.info("플레이어 강퇴 이벤트 수신: eventId={}, joinCode={}, playerName={}",
                    event.eventId(), event.joinCode(), event.playerName());

            final JoinCode joinCode = new JoinCode(event.joinCode());
            final Room room = roomQueryService.getByJoinCode(joinCode);

            final boolean removed = room.removePlayer(new PlayerName(event.playerName()));
            if (!removed) {
                log.warn("플레이어 강퇴 실패 - 플레이어 없음: eventId={}, joinCode={}, playerName={}",
                        event.eventId(), event.joinCode(), event.playerName());
                return;
            }

            if (room.isEmpty()) {
                roomCommandService.delete(joinCode);
                log.info("방이 비어 있어 삭제: joinCode={}", event.joinCode());
                return;
            }

            final List<PlayerResponse> responses = room.getPlayers().stream()
                    .map(PlayerResponse::from)
                    .toList();

            messagingTemplate.convertAndSend(
                    "/topic/room/" + event.joinCode(),
                    WebSocketResponse.success(responses)
            );
            log.info("플레이어 강퇴 이벤트 처리 완료: eventId={}, joinCode={}, playerName={}",
                    event.eventId(), event.joinCode(), event.playerName());

        } catch (Exception e) {
            log.error("플레이어 강퇴 이벤트 처리 실패", e);
            throw e;
        }
    }

    @Override
    public RoomEventType getSupportedEventType() {
        return RoomEventType.PLAYER_KICK;
    }

}
