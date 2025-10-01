package coffeeshout.minigame.infra.messaging.handler;

import coffeeshout.minigame.application.CardGameService;
import coffeeshout.minigame.domain.event.MiniGameEventType;
import coffeeshout.minigame.domain.event.StartMiniGameCommandEvent;
import coffeeshout.room.domain.JoinCode;
import coffeeshout.room.domain.Room;
import coffeeshout.room.domain.service.RoomQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StartMiniGameCommandEventHandler implements MiniGameEventHandler<StartMiniGameCommandEvent> {

    private final CardGameService cardGameService;
    private final RoomQueryService roomQueryService;

    @Override
    public void handle(StartMiniGameCommandEvent event) {
        try {
            log.info("미니게임 시작 이벤트 수신: eventId={}, joinCode={}, hostName={}",
                    event.eventId(), event.joinCode(), event.hostName());

            updateRoomStateAndStartGame(event);

        } catch (Exception e) {
            log.error("미니게임 시작 이벤트 처리 실패: eventId={}, joinCode={}",
                    event.eventId(), event.joinCode(), e);
        }
    }

    private void updateRoomStateAndStartGame(StartMiniGameCommandEvent event) {
        final Room room = roomQueryService.getByJoinCode(new JoinCode(event.joinCode()));
        room.startNextGame(event.hostName());

        cardGameService.startInternal(event.joinCode(), event.hostName());
    }

    @Override
    public MiniGameEventType getSupportedEventType() {
        return MiniGameEventType.START_MINIGAME_COMMAND;
    }
}
