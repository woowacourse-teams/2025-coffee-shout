package coffeeshout.minigame.infra.messaging.handler;

import coffeeshout.minigame.application.CardGameService;
import coffeeshout.minigame.domain.event.MiniGameEventType;
import coffeeshout.minigame.domain.event.SelectCardCommandEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SelectCardCommandEventHandler implements MiniGameEventHandler<SelectCardCommandEvent> {

    private final CardGameService cardGameService;

    @Override
    public void handle(SelectCardCommandEvent event) {
        try {
            log.info("카드 선택 이벤트 수신: eventId={}, joinCode={}, playerName={}, cardIndex={}", 
                    event.getEventId(), event.joinCode(), event.playerName(), event.cardIndex());

            cardGameService.selectCardInternal(event.joinCode(), event.playerName(), event.cardIndex());

            log.info("카드 선택 이벤트 처리 완료: eventId={}, joinCode={}", 
                    event.getEventId(), event.joinCode());

        } catch (Exception e) {
            log.error("카드 선택 이벤트 처리 실패: eventId={}, joinCode={}, playerName={}, cardIndex={}", 
                    event.getEventId(), event.joinCode(), event.playerName(), event.cardIndex(), e);
        }
    }

    @Override
    public MiniGameEventType getSupportedEventType() {
        return MiniGameEventType.SELECT_CARD_COMMAND;
    }
}
