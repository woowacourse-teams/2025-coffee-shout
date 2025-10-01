package coffeeshout.minigame.racinggame.infra.messaging.handler;

import coffeeshout.minigame.racinggame.application.RacingGameCommandService;
import coffeeshout.minigame.racinggame.domain.event.RacingGameEventType;
import coffeeshout.minigame.racinggame.domain.event.StartRacingGameCommandEvent;
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
public class StartRacingGameCommandEventHandler implements RacingGameEventHandler<StartRacingGameCommandEvent> {

    private final RacingGameCommandService racingGameCommandService;

    @Override
    public void handle(StartRacingGameCommandEvent event) {
        try {
            log.info("레이싱 게임 시작 이벤트 수신: eventId={}, joinCode={}, hostName={}",
                    event.eventId(), event.joinCode(), event.hostName());

            racingGameCommandService.startGame(event.joinCode(), event.hostName());

            log.info("레이싱 게임 시작 이벤트 처리 완료: eventId={}, joinCode={}",
                    event.eventId(), event.joinCode());

        } catch (Exception e) {
            log.error("레이싱 게임 시작 이벤트 처리 실패: eventId={}, joinCode={}",
                    event.eventId(), event.joinCode(), e);
        }
    }

    @Override
    public RacingGameEventType getSupportedEventType() {
        return RacingGameEventType.START_RACING_GAME_COMMAND;
    }
}
