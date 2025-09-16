package coffeeshout.minigame.application;

import static coffeeshout.minigame.domain.cardgame.CardGameTaskType.FIRST_ROUND_LOADING;

import coffeeshout.global.config.InstanceConfig;
import coffeeshout.global.redis.RedisMessagePublisher;
import coffeeshout.global.redis.event.minigame.CardSelectedEvent;
import coffeeshout.minigame.domain.dto.CardGameStartEvent;
import coffeeshout.minigame.domain.dto.CardSelectEvent;
import coffeeshout.minigame.domain.MiniGameType;
import coffeeshout.minigame.domain.cardgame.CardGame;
import coffeeshout.minigame.domain.cardgame.CardGameTaskExecutors;
import coffeeshout.minigame.domain.cardgame.CardGameTaskType;
import coffeeshout.minigame.common.task.TaskManager;
import coffeeshout.room.domain.JoinCode;
import coffeeshout.room.domain.Playable;
import coffeeshout.room.domain.Room;
import coffeeshout.room.domain.player.Player;
import coffeeshout.room.domain.player.PlayerName;
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
    private final RedisMessagePublisher messagePublisher;
    private final InstanceConfig instanceConfig;

    @Autowired
    public CardGameService(
            RoomQueryService roomQueryService,
            CardGameTaskExecutors cardGameTaskExecutors,
            @Qualifier("miniGameTaskScheduler") TaskScheduler scheduler,
            ApplicationEventPublisher eventPublisher,
            RedisMessagePublisher messagePublisher,
            InstanceConfig instanceConfig
    ) {
        this.roomQueryService = roomQueryService;
        this.cardGameTaskExecutors = cardGameTaskExecutors;
        this.scheduler = scheduler;
        this.eventPublisher = eventPublisher;
        this.messagePublisher = messagePublisher;
        this.instanceConfig = instanceConfig;
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
        final CardGame cardGame = getCardGame(roomJoinCode);
        final Player player = cardGame.findPlayerByName(new PlayerName(playerName));
        cardGame.selectCard(player, cardIndex);
        
        // 로컬 이벤트 발행
        eventPublisher.publishEvent(new CardSelectEvent(roomJoinCode, cardGame));
        
        // Redis 이벤트 발행 (다른 서버 인스턴스로 동기화)
        messagePublisher.publishCardSelected(new CardSelectedEvent(
                joinCode,
                playerName,
                cardIndex,
                instanceConfig.getInstanceId()
        ));
    }

    private CardGame getCardGame(JoinCode joinCode) {
        final Room room = roomQueryService.getByJoinCode(joinCode);
        return (CardGame) room.findMiniGame(MiniGameType.CARD_GAME);
    }
}
