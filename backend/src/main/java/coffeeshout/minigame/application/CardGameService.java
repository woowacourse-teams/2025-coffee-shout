package coffeeshout.minigame.application;

import static coffeeshout.minigame.domain.task.CardGameTaskType.FIRST_ROUND_LOADING;

import coffeeshout.global.ui.WebSocketResponse;
import coffeeshout.global.websocket.LoggingSimpMessagingTemplate;
import coffeeshout.minigame.domain.MiniGameType;
import coffeeshout.minigame.domain.cardgame.CardGame;
import coffeeshout.minigame.domain.cardgame.CardGameTaskExecutors;
import coffeeshout.minigame.domain.task.CardGameTaskType;
import coffeeshout.minigame.domain.task.MiniGameTaskManager;
import coffeeshout.minigame.ui.response.MiniGameStartMessage;
import coffeeshout.minigame.ui.response.MiniGameStateMessage;
import coffeeshout.room.domain.JoinCode;
import coffeeshout.room.domain.Playable;
import coffeeshout.room.domain.Room;
import coffeeshout.room.domain.player.Player;
import coffeeshout.room.domain.player.PlayerName;
import coffeeshout.room.domain.service.RoomQueryService;
import java.util.Arrays;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

@Service
public class CardGameService implements MiniGameService {

    private static final String CARD_GAME_STATE_DESTINATION_FORMAT = "/topic/room/%s/gameState";
    private static final String GAME_START_DESTINATION_FORMAT = "/topic/room/%s/round";

    private final RoomQueryService roomQueryService;
    private final LoggingSimpMessagingTemplate messagingTemplate;

    private final CardGameTaskExecutors cardGameTaskExecutors;
    private final TaskScheduler scheduler;

    @Autowired
    public CardGameService(
            RoomQueryService roomQueryService,
            LoggingSimpMessagingTemplate messagingTemplate,
            CardGameTaskExecutors cardGameTaskExecutors,
            @Qualifier("miniGameTaskScheduler") TaskScheduler scheduler
    ) {
        this.roomQueryService = roomQueryService;
        this.messagingTemplate = messagingTemplate;
        this.cardGameTaskExecutors = cardGameTaskExecutors;
        this.scheduler = scheduler;
    }

    @Override
    public void start(Playable playable, String joinCode) {
        sendGameStartMessage(joinCode, playable.getMiniGameType());
        final JoinCode roomJoinCode = new JoinCode(joinCode);
        final Room room = roomQueryService.findByJoinCode(roomJoinCode);
        final CardGame cardGame = (CardGame) playable;
        final MiniGameTaskManager<CardGameTaskType> manager = new MiniGameTaskManager<>(scheduler);
        cardGameTaskExecutors.put(roomJoinCode, manager);
        Arrays.stream(CardGameTaskType.values()).forEach(type -> manager.addTask(
                type,
                type.createTask(cardGame, room, () -> sendCardGameState(roomJoinCode))
        ));
        manager.startWith(FIRST_ROUND_LOADING);
    }

    public void selectCard(String joinCode, String playerName, Integer cardIndex) {
        final JoinCode roomJoinCode = new JoinCode(joinCode);
        final CardGame cardGame = getCardGame(roomJoinCode);
        final Player player = cardGame.findPlayerByName(new PlayerName(playerName));
        cardGame.selectCard(player, cardIndex);
        sendCardGameState(roomJoinCode);
    }

    private void sendCardGameState(JoinCode joinCode) {
        final CardGame cardGame = getCardGame(joinCode);
        final MiniGameStateMessage message = MiniGameStateMessage.from(cardGame);
        final String destination = String.format(CARD_GAME_STATE_DESTINATION_FORMAT, joinCode.value());
        messagingTemplate.convertAndSend(destination, WebSocketResponse.success(message));
    }


    private void sendGameStartMessage(String joinCode, MiniGameType miniGameType) {
        messagingTemplate.convertAndSend(
                String.format(GAME_START_DESTINATION_FORMAT, joinCode),
                WebSocketResponse.success(new MiniGameStartMessage(miniGameType))
        );
    }

    private CardGame getCardGame(JoinCode joinCode) {
        final Room room = roomQueryService.findByJoinCode(joinCode);
        return (CardGame) room.findMiniGame(MiniGameType.CARD_GAME);
    }
}
