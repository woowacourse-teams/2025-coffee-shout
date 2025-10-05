package coffeeshout.minigame.infra.messaging.handler;

import coffeeshout.global.lock.RedisLock;
import coffeeshout.minigame.application.CardGameService;
import coffeeshout.minigame.domain.event.MiniGameEventType;
import coffeeshout.minigame.domain.event.StartMiniGameCommandEvent;
import coffeeshout.room.domain.JoinCode;
import coffeeshout.room.domain.Room;
import coffeeshout.room.domain.service.RoomQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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
            
            tryDbSave(event);

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

    @RedisLock(
            key = "#event.eventId()",
            lockPrefix = "event:lock:",
            donePrefix = "event:done:",
            waitTime = 0,
            leaseTime = 5000
    )
    @Transactional
    public void tryDbSave(StartMiniGameCommandEvent event) {
        cardGameService.saveGameEntities(event.joinCode());
        log.info("미니게임 시작 이벤트 처리 완료 (DB 저장): eventId={}, joinCode={}",
                event.eventId(), event.joinCode());
    }

    @Override
    public MiniGameEventType getSupportedEventType() {
        return MiniGameEventType.START_MINIGAME_COMMAND;
    }
}
