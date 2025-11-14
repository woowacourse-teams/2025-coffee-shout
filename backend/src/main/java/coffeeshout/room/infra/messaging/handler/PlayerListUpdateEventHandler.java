package coffeeshout.room.infra.messaging.handler;

import coffeeshout.room.application.RoomEventHandler;
import coffeeshout.room.domain.event.PlayerListUpdateEvent;
import coffeeshout.room.domain.event.RoomEventType;
import coffeeshout.room.domain.event.broadcast.PlayerListChangedEvent;
import coffeeshout.room.domain.player.Player;
import coffeeshout.room.domain.service.PlayerCommandService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PlayerListUpdateEventHandler implements RoomEventHandler<PlayerListUpdateEvent> {

    private final PlayerCommandService playerCommandService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void handle(PlayerListUpdateEvent event) {
        try {
            log.info("플레이어 목록 업데이트 이벤트 수신: eventId={}, joinCode={}",
                    event.eventId(), event.joinCode());

            final List<Player> players = playerCommandService.getAllPlayers(event.joinCode());

            // Spring Domain Event 발행 - RoomMessagePublisher가 브로드캐스트 처리
            eventPublisher.publishEvent(new PlayerListChangedEvent(event.joinCode(), players));

            log.info("플레이어 목록 업데이트 이벤트 처리 완료: eventId={}, joinCode={}",
                    event.eventId(), event.joinCode());

        } catch (Exception e) {
            log.error("플레이어 목록 업데이트 이벤트 처리 실패", e);
        }
    }

    @Override
    public RoomEventType getSupportedEventType() {
        return RoomEventType.PLAYER_LIST_UPDATE;
    }
}
