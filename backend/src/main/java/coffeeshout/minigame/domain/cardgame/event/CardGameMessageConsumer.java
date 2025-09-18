package coffeeshout.minigame.domain.cardgame.event;

import coffeeshout.minigame.domain.MiniGameType;
import coffeeshout.minigame.domain.cardgame.CardGame;
import coffeeshout.minigame.domain.cardgame.CardGameTaskType;
import coffeeshout.minigame.domain.cardgame.event.dto.CardGameStartMessage;
import coffeeshout.minigame.domain.cardgame.event.dto.CardGameStateChangeMessage;
import coffeeshout.room.domain.JoinCode;
import coffeeshout.room.domain.Room;
import coffeeshout.room.domain.service.RoomQueryService;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

/*
    redis pub/sub 도입시 메시지를 처리하게될 클래스
 */

@Component
public class CardGameMessageConsumer {

    private final TaskScheduler taskScheduler;
    private final ApplicationEventPublisher publisher;
    private final RoomQueryService roomQueryService;

    public CardGameMessageConsumer(
            @Qualifier("miniGameTaskScheduler") TaskScheduler taskScheduler,
            ApplicationEventPublisher publisher,
            RoomQueryService roomQueryService
    ) {
        this.taskScheduler = taskScheduler;
        this.publisher = publisher;
        this.roomQueryService = roomQueryService;
    }

    @EventListener
    public void consumeCardGameStateChangeMessage(CardGameStateChangeMessage cardGameStateChangeMessage) {
        final JoinCode joinCode = new JoinCode(cardGameStateChangeMessage.joinCode());
        final Room room = roomQueryService.getByJoinCode(joinCode);
        final CardGame cardGame = getCardGame(joinCode);
        final CardGameTaskType currentTask = CardGameTaskType.valueOf(cardGameStateChangeMessage.currentTaskName());
        if (currentTask.isLastTask()) {
            return;
        }
        final CardGameTaskType nextTask = currentTask.nextTask();
        final Instant instant = Instant.now().plusMillis(currentTask.getState().getDurationMillis().toMillis());
        final Runnable runnable = nextTask.createTask(
                cardGame,
                room,
                publisher
        );
        taskScheduler.schedule(runnable, instant);
    }

    @EventListener
    public void consumeCardGameStartMessage(CardGameStartMessage cardGameStartMessage) {
        final JoinCode joinCode = new JoinCode(cardGameStartMessage.joinCode());
        final Room room = roomQueryService.getByJoinCode(joinCode);
        final CardGame cardGame = getCardGame(joinCode);
        final CardGameTaskType currentTask = CardGameTaskType.valueOf(cardGameStartMessage.cardGameTaskType());
        final Runnable runnable = currentTask.createTask(
                cardGame,
                room,
                publisher
        );
        taskScheduler.schedule(runnable, Instant.now());
    }

    private CardGame getCardGame(JoinCode joinCode) {
        final Room room = roomQueryService.getByJoinCode(joinCode);
        return (CardGame) room.findMiniGame(MiniGameType.CARD_GAME);
    }
}
