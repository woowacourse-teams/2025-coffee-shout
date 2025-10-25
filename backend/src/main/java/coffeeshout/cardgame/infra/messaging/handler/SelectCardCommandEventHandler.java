package coffeeshout.cardgame.infra.messaging.handler;

import coffeeshout.cardgame.application.CardGameService;
import coffeeshout.cardgame.domain.event.SelectCardCommandEvent;
import coffeeshout.global.redis.BaseEvent;
import coffeeshout.global.redis.EventHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SelectCardCommandEventHandler implements EventHandler {

    private final CardGameService cardGameService;

    @Override
    public void handle(BaseEvent event) {
        final SelectCardCommandEvent selectCardCommandEvent = (SelectCardCommandEvent) event;
        cardGameService.selectCard(selectCardCommandEvent.joinCode(), selectCardCommandEvent.playerName(), selectCardCommandEvent.cardIndex());
    }

    @Override
    public Class<?> eventType() {
        return SelectCardCommandEvent.class;
    }
}
