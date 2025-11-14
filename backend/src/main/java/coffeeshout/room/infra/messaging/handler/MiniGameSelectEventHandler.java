package coffeeshout.room.infra.messaging.handler;

import coffeeshout.minigame.domain.MiniGameType;
import coffeeshout.room.application.port.RoomEventHandler;
import coffeeshout.room.domain.event.MiniGameSelectEvent;
import coffeeshout.room.domain.event.RoomEventType;
import coffeeshout.room.domain.event.broadcast.MiniGameListChangedEvent;
import coffeeshout.room.domain.service.MiniGameCommandService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MiniGameSelectEventHandler implements RoomEventHandler<MiniGameSelectEvent> {

    private final MiniGameCommandService miniGameCommandService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void handle(MiniGameSelectEvent event) {
        try {
            log.info("미니게임 선택 이벤트 수신: eventId={}, joinCode={}, hostName={}, miniGameTypes={}",
                    event.eventId(), event.joinCode(), event.hostName(), event.miniGameTypes());

            final List<MiniGameType> selectedMiniGames = miniGameCommandService.updateMiniGames(
                    event.joinCode(), event.hostName(), event.miniGameTypes());

            // Spring Domain Event 발행 - RoomMessagePublisher가 브로드캐스트 처리
            eventPublisher.publishEvent(new MiniGameListChangedEvent(event.joinCode(), selectedMiniGames));

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
