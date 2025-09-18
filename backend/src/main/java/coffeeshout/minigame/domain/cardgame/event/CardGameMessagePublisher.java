package coffeeshout.minigame.domain.cardgame.event;

import coffeeshout.minigame.domain.cardgame.CardGameTaskType;
import coffeeshout.minigame.domain.cardgame.event.dto.CardGameStartMessage;
import coffeeshout.minigame.domain.cardgame.event.dto.CardGameStartedEvent;
import coffeeshout.minigame.domain.cardgame.event.dto.CardGameStateChangedEvent;
import coffeeshout.minigame.domain.cardgame.event.dto.CardGameStateChangeMessage;
import coffeeshout.room.domain.JoinCode;
import coffeeshout.room.domain.Room;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/*
    TODO: redis도입시 event발송에서 redis pub/sub message 발송으로 변경하기
 */

@Component
public class CardGameMessagePublisher {

    private final ApplicationEventPublisher publisher;

    public CardGameMessagePublisher(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    @EventListener
    public void publishCardGameStateChanged(CardGameStateChangedEvent cardGameStateChangedEvent) {
        final Room room = cardGameStateChangedEvent.room();
        publisher.publishEvent(new CardGameStateChangeMessage(
                room.getJoinCode().getValue(),
                cardGameStateChangedEvent.currentTask().name())
        );
    }

    @EventListener
    public void publishCardGameStarted(CardGameStartedEvent cardGameStartedEvent) {
        JoinCode joinCode = cardGameStartedEvent.joinCode();
        publisher.publishEvent(new CardGameStartMessage(
                joinCode.getValue(),
                CardGameTaskType.getFirstTask().name()
        ));
    }
}
