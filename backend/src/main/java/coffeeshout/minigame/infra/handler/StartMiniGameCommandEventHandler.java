package coffeeshout.minigame.infra.handler;

import coffeeshout.minigame.application.CardGameService;
import coffeeshout.minigame.domain.event.MiniGameEventType;
import coffeeshout.minigame.domain.event.StartMiniGameCommandEvent;
import coffeeshout.minigame.infra.MiniGameEventWaitManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StartMiniGameCommandEventHandler implements MiniGameEventHandler<StartMiniGameCommandEvent> {

    private final CardGameService cardGameService;
    private final MiniGameEventWaitManager miniGameEventWaitManager;

    @Override
    public void handle(StartMiniGameCommandEvent event) {
        try {
            log.info("미니게임 시작 이벤트 수신: eventId={}, joinCode={}, hostName={}", 
                    event.getEventId(), event.joinCode(), event.hostName());

            cardGameService.startInternal(event.joinCode(), event.hostName());

            miniGameEventWaitManager.notifySuccess(event.getEventId(), null);

            log.info("미니게임 시작 이벤트 처리 완료: eventId={}, joinCode={}", 
                    event.getEventId(), event.joinCode());

        } catch (Exception e) {
            log.error("미니게임 시작 이벤트 처리 실패: eventId={}, joinCode={}", 
                    event.getEventId(), event.joinCode(), e);
            miniGameEventWaitManager.notifyFailure(event.getEventId(), e);
        }
    }

    @Override
    public MiniGameEventType getSupportedEventType() {
        return MiniGameEventType.START_MINIGAME_COMMAND;
    }
}
