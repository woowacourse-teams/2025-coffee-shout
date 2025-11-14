package coffeeshout.room.infra.messaging.handler;

import coffeeshout.room.application.RoomEventHandler;
import coffeeshout.room.domain.event.PlayerReadyEvent;
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
public class PlayerReadyEventHandler implements RoomEventHandler<PlayerReadyEvent> {

    private final PlayerCommandService playerCommandService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void handle(PlayerReadyEvent event) {
        try {
            log.info("플레이어 ready 이벤트 수신: eventId={}, joinCode={}, playerName={}, isReady={}",
                    event.eventId(), event.joinCode(), event.playerName(), event.isReady());

            final List<Player> players = playerCommandService.changePlayerReadyState(
                    event.joinCode(), event.playerName(), event.isReady());

            // Spring Domain Event 발행 - RoomMessagePublisher가 브로드캐스트 처리
            eventPublisher.publishEvent(new PlayerListChangedEvent(event.joinCode(), players));

            log.info("플레이어 ready 이벤트 처리 완료: eventId={}, joinCode={}, playerName={}, isReady={}",
                    event.eventId(), event.joinCode(), event.playerName(), event.isReady());

        } catch (Exception e) {
            log.error("플레이어 ready 이벤트 처리 실패", e);
        }
    }

    @Override
    public RoomEventType getSupportedEventType() {
        return RoomEventType.PLAYER_READY;
    }
}
