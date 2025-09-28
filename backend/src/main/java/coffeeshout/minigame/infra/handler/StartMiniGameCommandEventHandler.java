package coffeeshout.minigame.infra.handler;

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
                    event.getEventId(), event.getJoinCode(), event.getHostName());

            // Room 상태 먼저 변경
            final Room room = roomQueryService.getByJoinCode(new JoinCode(event.getJoinCode()));
            room.startNextGame(event.getHostName());

            // 카드게임 시작
            cardGameService.startInternal(event.getJoinCode(), event.getHostName());

            log.info("미니게임 시작 이벤트 처리 완료: eventId={}, joinCode={}",
                    event.getEventId(), event.getJoinCode());

        } catch (Exception e) {
            log.error("미니게임 시작 이벤트 처리 실패: eventId={}, joinCode={}",
                    event.getEventId(), event.getJoinCode(), e);
        }
    }

    @Override
    public MiniGameEventType getSupportedEventType() {
        return MiniGameEventType.START_MINIGAME_COMMAND;
    }
}
