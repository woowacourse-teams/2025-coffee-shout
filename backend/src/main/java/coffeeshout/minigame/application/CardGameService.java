package coffeeshout.minigame.application;

import static coffeeshout.minigame.domain.cardgame.CardGameTaskType.FIRST_ROUND_LOADING;

import coffeeshout.minigame.common.task.TaskManager;
import coffeeshout.minigame.domain.MiniGameResult;
import coffeeshout.minigame.domain.MiniGameScore;
import coffeeshout.minigame.domain.cardgame.CardGame;
import coffeeshout.minigame.domain.cardgame.CardGameTaskExecutors;
import coffeeshout.minigame.domain.cardgame.CardGameTaskType;
import coffeeshout.minigame.domain.cardgame.card.CardGameRandomDeckGenerator;
import coffeeshout.minigame.domain.cardgame.service.CardGameCommandService;
import coffeeshout.minigame.domain.cardgame.service.CardGameQueryService;
import coffeeshout.minigame.domain.dto.CardGameStartEvent;
import coffeeshout.minigame.domain.dto.CardSelectEvent;
import coffeeshout.room.domain.JoinCode;
import coffeeshout.room.domain.player.Player;
import coffeeshout.room.domain.player.PlayerName;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

@Service
public class CardGameService implements MiniGameService {

    private final CardGameQueryService cardGameQueryService;
    private final CardGameCommandService cardGameCommandService;
    private final CardGameTaskExecutors cardGameTaskExecutors;
    private final TaskScheduler scheduler;
    private final ApplicationEventPublisher eventPublisher;

    @Autowired
    public CardGameService(
            CardGameQueryService cardGameQueryService,
            CardGameCommandService cardGameCommandService,
            CardGameTaskExecutors cardGameTaskExecutors,
            @Qualifier("miniGameTaskScheduler") TaskScheduler scheduler,
            ApplicationEventPublisher eventPublisher
    ) {
        this.cardGameQueryService = cardGameQueryService;
        this.cardGameCommandService = cardGameCommandService;
        this.cardGameTaskExecutors = cardGameTaskExecutors;
        this.scheduler = scheduler;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void start(String joinCode, List<Player> players) {
        final JoinCode roomJoinCode = new JoinCode(joinCode);
        final CardGame cardGame = new CardGame(players, roomJoinCode, new CardGameRandomDeckGenerator());
        cardGameCommandService.save(cardGame);
        final TaskManager<CardGameTaskType> manager = new TaskManager<>(scheduler);
        cardGameTaskExecutors.put(roomJoinCode, manager);
        eventPublisher.publishEvent(new CardGameStartEvent(roomJoinCode, cardGame));
        Arrays.stream(CardGameTaskType.values()).forEach(type -> manager.addTask(
                type,
                type.createTask(roomJoinCode, eventPublisher, cardGameQueryService, cardGameCommandService)
        ));
        manager.startWith(FIRST_ROUND_LOADING);
    }

    @Override
    public Map<Player, MiniGameScore> getMiniGameScores(String joinCode) {
        final JoinCode roomJoinCode = new JoinCode(joinCode);
        final CardGame cardGame = cardGameQueryService.getByJoinCode(roomJoinCode);
        return cardGame.getScores();
    }

    @Override
    public MiniGameResult getMiniGameRanks(String joinCode) {
        final JoinCode roomJoinCode = new JoinCode(joinCode);
        final CardGame cardGame = cardGameQueryService.getByJoinCode(roomJoinCode);
        return cardGame.getResult();
    }

    public void selectCard(String joinCode, String playerName, Integer cardIndex) {
        final JoinCode roomJoinCode = new JoinCode(joinCode);
        final CardGame cardGame = cardGameQueryService.getByJoinCode(roomJoinCode);
        cardGame.selectCard(new PlayerName(playerName), cardIndex);
        cardGameCommandService.save(cardGame);
        eventPublisher.publishEvent(new CardSelectEvent(roomJoinCode, cardGame));
    }
}
