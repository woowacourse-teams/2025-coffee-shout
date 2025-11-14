package coffeeshout.room.infra.messaging.handler;

import coffeeshout.global.ui.WebSocketResponse;
import coffeeshout.global.websocket.LoggingSimpMessagingTemplate;
import coffeeshout.room.application.RoomEventHandler;
import coffeeshout.room.domain.event.PlayerKickEvent;
import coffeeshout.room.domain.event.RoomEventType;
import coffeeshout.room.domain.player.Player;
import coffeeshout.room.domain.service.PlayerCommandService;
import coffeeshout.room.ui.response.PlayerResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PlayerKickEventHandler implements RoomEventHandler<PlayerKickEvent> {

    private final PlayerCommandService playerCommandService;
    private final LoggingSimpMessagingTemplate messagingTemplate;

    @Override
    public void handle(PlayerKickEvent event) {
        try {
            log.info("플레이어 강퇴 이벤트 수신: eventId={}, joinCode={}, playerName={}",
                    event.eventId(), event.joinCode(), event.playerName());

            final boolean removed = playerCommandService.removePlayer(event.joinCode(), event.playerName());
            if (!removed) {
                log.warn("플레이어 강퇴 실패 - 플레이어 없음: eventId={}, joinCode={}, playerName={}",
                        event.eventId(), event.joinCode(), event.playerName());
                return;
            }

            // removePlayer가 방을 삭제했다면 (empty room) getAllPlayers가 실패할 수 있으므로 확인
            try {
                final List<Player> players = playerCommandService.getAllPlayers(event.joinCode());
                final List<PlayerResponse> responses = players.stream()
                        .map(PlayerResponse::from)
                        .toList();

                messagingTemplate.convertAndSend(
                        "/topic/room/" + event.joinCode(),
                        WebSocketResponse.success(responses)
                );
            } catch (Exception e) {
                log.info("방이 비어 있어 삭제됨: joinCode={}", event.joinCode());
                return;
            }

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
