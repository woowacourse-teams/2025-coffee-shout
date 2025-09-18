package coffeeshout.minigame.domain.cardgame.event;

import coffeeshout.minigame.domain.cardgame.CardGameTaskType;
import coffeeshout.minigame.domain.cardgame.event.dto.CardGameStartProcessEvent;
import coffeeshout.minigame.domain.cardgame.event.dto.CardGameStartedEvent;
import coffeeshout.minigame.domain.cardgame.event.dto.CardGameStateChangedEvent;
import coffeeshout.minigame.domain.cardgame.event.dto.CardGameStateDoneEvent;
import coffeeshout.room.domain.JoinCode;
import coffeeshout.room.domain.Room;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class CardGamePublishEventListener {

    private final ApplicationEventPublisher publisher;

    public CardGamePublishEventListener(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    @EventListener
    public void publishCardGameStateChanged(CardGameStateChangedEvent cardGameStateChangedEvent) {
        final Room room = cardGameStateChangedEvent.room();
        publisher.publishEvent(new CardGameStateDoneEvent(
                room.getJoinCode().getValue(),
                cardGameStateChangedEvent.currentTask().name())
        );
    }

    @EventListener
    public void publishCardGameStarted(CardGameStartedEvent cardGameStartedEvent) {
        JoinCode joinCode = cardGameStartedEvent.joinCode();
        publisher.publishEvent(new CardGameStartProcessEvent(
                joinCode.getValue(),
                CardGameTaskType.getFirstTask().name()
        ));
    }
}
