package coffeeshout.minigame.cardgame.infra.messaging.handler;

import coffeeshout.minigame.cardgame.application.CardGameService;
import coffeeshout.minigame.cardgame.domain.event.MiniGameEventType;
import coffeeshout.minigame.cardgame.domain.event.StartMiniGameCommandEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Redis 이벤트를 수신하여 Command Service로 라우팅하는 핸들러
 * 메시지 수신과 로깅만 담당하며, 비즈니스 로직은 CommandService에 위임합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StartMiniGameCommandEventHandler implements MiniGameEventHandler<StartMiniGameCommandEvent> {

    private final CardGameService cardGameService;

    @Override
    public void handle(StartMiniGameCommandEvent event) {
        try {
            log.info("미니게임 시작 이벤트 수신: eventId={}, joinCode={}, hostName={}",
                    event.getEventId(), event.joinCode(), event.hostName());

            cardGameService.startInternal(event.joinCode(), event.hostName());

            log.info("미니게임 시작 이벤트 처리 완료: eventId={}, joinCode={}",
                    event.getEventId(), event.joinCode());

        } catch (Exception e) {
            log.error("미니게임 시작 이벤트 처리 실패: eventId={}, joinCode={}",
                    event.getEventId(), event.joinCode(), e);
        }
    }

    @Override
    public MiniGameEventType getSupportedEventType() {
        return MiniGameEventType.START_MINIGAME_COMMAND;
    }
}
