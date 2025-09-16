package coffeeshout.minigame.application;

import static coffeeshout.minigame.domain.cardgame.CardGameTaskType.FIRST_ROUND_LOADING;

import coffeeshout.minigame.common.task.TaskManager;
import coffeeshout.minigame.domain.MiniGameType;
import coffeeshout.minigame.domain.cardgame.CardGame;
import coffeeshout.minigame.domain.cardgame.CardGameTaskExecutors;
import coffeeshout.minigame.domain.cardgame.CardGameTaskType;
import coffeeshout.minigame.domain.dto.CardGameStartEvent;
import coffeeshout.minigame.domain.dto.CardSelectEvent;
import coffeeshout.room.domain.JoinCode;
import coffeeshout.room.domain.Playable;
import coffeeshout.room.domain.Room;
import coffeeshout.room.domain.player.PlayerName;
import coffeeshout.room.domain.service.RoomCommandService;
import coffeeshout.room.domain.service.RoomQueryService;
import java.util.Arrays;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

@Service
public class CardGameService implements MiniGameService {

    private final RoomQueryService roomQueryService;
    private final CardGameTaskExecutors cardGameTaskExecutors;
    private final TaskScheduler scheduler;
    private final ApplicationEventPublisher eventPublisher;
    private final RoomCommandService roomCommandService;

    @Autowired
    public CardGameService(
            RoomQueryService roomQueryService,
            CardGameTaskExecutors cardGameTaskExecutors,
            @Qualifier("miniGameTaskScheduler") TaskScheduler scheduler,
            ApplicationEventPublisher eventPublisher,
            RoomCommandService roomCommandService
    ) {
        this.roomQueryService = roomQueryService;
        this.cardGameTaskExecutors = cardGameTaskExecutors;
        this.scheduler = scheduler;
        this.eventPublisher = eventPublisher;
        this.roomCommandService = roomCommandService;
    }

    @Override
    public void start(Playable playable, String joinCode) {
        final JoinCode roomJoinCode = new JoinCode(joinCode);
        final Room room = roomQueryService.getByJoinCode(roomJoinCode);
        final CardGame cardGame = (CardGame) playable;
        final TaskManager<CardGameTaskType> manager = new TaskManager<>(scheduler);
        cardGameTaskExecutors.put(roomJoinCode, manager);
        eventPublisher.publishEvent(new CardGameStartEvent(roomJoinCode, cardGame));
        Arrays.stream(CardGameTaskType.values()).forEach(type -> manager.addTask(
                type,
                type.createTask(cardGame, room, eventPublisher)
        ));
        manager.startWith(FIRST_ROUND_LOADING);
    }

    public void selectCard(String joinCode, String playerName, Integer cardIndex) {
        final JoinCode roomJoinCode = new JoinCode(joinCode);
        final Room room = roomQueryService.getByJoinCode(roomJoinCode);
        final CardGame cardGame = (CardGame) room.findMiniGame(MiniGameType.CARD_GAME);
        cardGame.selectCard(new PlayerName(playerName), cardIndex);

        eventPublisher.publishEvent(new CardSelectEvent(roomJoinCode, cardGame));
        roomCommandService.save(room);
    }
}
