package coffeeshout.minigame.application;

import static coffeeshout.minigame.domain.temp.CardGameTaskType.FIRST_ROUND_LOADING_DELAY;
import static coffeeshout.minigame.domain.temp.CardGameTaskType.FIRST_ROUND_LOADING_STATE;
import static coffeeshout.minigame.domain.temp.CardGameTaskType.FIRST_ROUND_PLAYING_DELAY;
import static coffeeshout.minigame.domain.temp.CardGameTaskType.FIRST_ROUND_PLAYING_STATE;
import static coffeeshout.minigame.domain.temp.CardGameTaskType.FIRST_ROUND_SCORE_BOARD_DELAY;
import static coffeeshout.minigame.domain.temp.CardGameTaskType.FIRST_ROUND_SCORE_BOARD_STATE;
import static coffeeshout.minigame.domain.temp.CardGameTaskType.GAME_FINISH_STATE;
import static coffeeshout.minigame.domain.temp.CardGameTaskType.SECOND_ROUND_LOADING_DELAY;
import static coffeeshout.minigame.domain.temp.CardGameTaskType.SECOND_ROUND_LOADING_STATE;
import static coffeeshout.minigame.domain.temp.CardGameTaskType.SECOND_ROUND_PLAYING_DELAY;
import static coffeeshout.minigame.domain.temp.CardGameTaskType.SECOND_ROUND_PLAYING_STATE;
import static coffeeshout.minigame.domain.temp.CardGameTaskType.SECOND_ROUND_SCORE_BOARD_DELAY;
import static coffeeshout.minigame.domain.temp.CardGameTaskType.SECOND_ROUND_SCORE_BOARD_STATE;

import coffeeshout.global.ui.WebSocketResponse;
import coffeeshout.minigame.domain.MiniGameType;
import coffeeshout.minigame.domain.cardgame.CardGame;
import coffeeshout.minigame.domain.cardgame.CardGameTaskExecutorsV2;
import coffeeshout.minigame.domain.temp.CardGameTaskType;
import coffeeshout.minigame.domain.temp.ChainedTask;
import coffeeshout.minigame.domain.temp.MiniGameTaskManager;
import coffeeshout.minigame.ui.response.MinIGameStartMessage;
import coffeeshout.minigame.ui.response.MiniGameRanksMessage;
import coffeeshout.minigame.ui.response.MiniGameScoresMessage;
import coffeeshout.minigame.ui.response.MiniGameStateMessage;
import coffeeshout.room.domain.JoinCode;
import coffeeshout.room.domain.Playable;
import coffeeshout.room.domain.Room;
import coffeeshout.room.domain.player.Player;
import coffeeshout.room.domain.player.PlayerName;
import coffeeshout.room.domain.service.RoomQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

@Service
public class CardGameService implements MiniGameService {

    private static final String CARD_GAME_STATE_DESTINATION_FORMAT = "/topic/room/%s/gameState";
    private static final String CARD_GAME_SCORE_DESTINATION_FORMAT = "/topic/room/%s/score";
    private static final String CARD_GAME_RESULT_DESTINATION_FORMAT = "/topic/room/%s/rank";
    private static final String GAME_START_DESTINATION_FORMAT = "/topic/room/%s/round";

    private final RoomQueryService roomQueryService;
    private final SimpMessagingTemplate messagingTemplate;

    private final CardGameTaskExecutorsV2 cardGameTaskExecutors;
    private final TaskScheduler scheduler;

    @Autowired
    public CardGameService(
            RoomQueryService roomQueryService,
            SimpMessagingTemplate messagingTemplate,
            CardGameTaskExecutorsV2 cardGameTaskExecutors,
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
        ChainedTask task1 = FIRST_ROUND_LOADING_STATE.createTask(cardGame, room, () -> sendCardGameState(roomJoinCode));
        ChainedTask task2 = FIRST_ROUND_LOADING_DELAY.createTask(cardGame, room, () -> sendCardGameState(roomJoinCode));
        ChainedTask task3 = FIRST_ROUND_PLAYING_STATE.createTask(cardGame, room, () -> sendCardGameState(roomJoinCode));
        ChainedTask task4 = FIRST_ROUND_PLAYING_DELAY.createTask(cardGame, room, () -> sendCardGameState(roomJoinCode));
        ChainedTask task5 = FIRST_ROUND_SCORE_BOARD_STATE.createTask(cardGame, room, () -> sendCardGameState(roomJoinCode));
        ChainedTask task6 = FIRST_ROUND_SCORE_BOARD_DELAY.createTask(cardGame, room, () -> sendCardGameState(roomJoinCode));
        ChainedTask task7 = SECOND_ROUND_LOADING_STATE.createTask(cardGame, room, () -> sendCardGameState(roomJoinCode));
        ChainedTask task8 = SECOND_ROUND_LOADING_DELAY.createTask(cardGame, room, () -> sendCardGameState(roomJoinCode));
        ChainedTask task9 = SECOND_ROUND_PLAYING_STATE.createTask(cardGame, room, () -> sendCardGameState(roomJoinCode));
        ChainedTask task10 = SECOND_ROUND_PLAYING_DELAY.createTask(cardGame, room, () -> sendCardGameState(roomJoinCode));
        ChainedTask task11 = SECOND_ROUND_SCORE_BOARD_STATE.createTask(cardGame, room, () -> sendCardGameState(roomJoinCode));
        ChainedTask task12 = SECOND_ROUND_SCORE_BOARD_DELAY.createTask(cardGame, room, () -> sendCardGameState(roomJoinCode));
        ChainedTask task13 = GAME_FINISH_STATE.createTask(cardGame, room, () -> sendCardGameResult(roomJoinCode));
        manager.addTask(FIRST_ROUND_LOADING_STATE, task1);
        manager.addTask(FIRST_ROUND_LOADING_DELAY, task2);
        manager.addTask(FIRST_ROUND_PLAYING_STATE, task3);
        manager.addTask(FIRST_ROUND_PLAYING_DELAY, task4);
        manager.addTask(FIRST_ROUND_SCORE_BOARD_STATE, task5);
        manager.addTask(FIRST_ROUND_SCORE_BOARD_DELAY, task6);
        manager.addTask(SECOND_ROUND_LOADING_STATE, task7);
        manager.addTask(SECOND_ROUND_LOADING_DELAY, task8);
        manager.addTask(SECOND_ROUND_PLAYING_STATE, task9);
        manager.addTask(SECOND_ROUND_PLAYING_DELAY, task10);
        manager.addTask(SECOND_ROUND_SCORE_BOARD_STATE, task11);
        manager.addTask(SECOND_ROUND_SCORE_BOARD_DELAY, task12);
        manager.addTask(GAME_FINISH_STATE, task13);
        manager.startWith(FIRST_ROUND_LOADING_STATE);
    }

//    @Override
//    public void start(Playable playable, String joinCode) {
//        sendGameStartMessage(joinCode, playable.getMiniGameType());
//        final JoinCode roomJoinCode = new JoinCode(joinCode);
//        final Room room = roomQueryService.findByJoinCode(roomJoinCode);
//        final CardGame cardGame = (CardGame) playable;
//        final TaskExecutor<CardGameTaskInfo> executor = new TaskExecutor<>();
//        cardGameTaskExecutors.put(roomJoinCode, executor);
//        executor.submits(List.of(
//                new Task<>(
//                        CardGameTaskInfo.WAITING_FOR_START,
//                        gameTaskFactory.loading(cardGame, () -> sendCardGameState(roomJoinCode))
//                ),
//                new Task<>(
//                        CardGameTaskInfo.FIRST_ROUND_PLAYING,
//                        gameTaskFactory.play(cardGame, () -> sendCardGameState(roomJoinCode))
//                ),
//                new Task<>(
//                        CardGameTaskInfo.FIRST_ROUND_SCORE_BOARD,
//                        gameTaskFactory.scoreBoard(cardGame, () -> sendCardGameState(roomJoinCode))
//                ),
//                new Task<>(
//                        CardGameTaskInfo.FIRST_ROUND_LOADING,
//                        gameTaskFactory.loading(cardGame, () -> sendCardGameState(roomJoinCode))
//                ),
//                new Task<>(
//                        CardGameTaskInfo.SECOND_ROUND_PLAYING,
//                        gameTaskFactory.play(cardGame, () -> sendCardGameState(roomJoinCode))
//                ),
//                new Task<>(
//                        CardGameTaskInfo.SECOND_ROUND_SCORE_BOARD,
//                        gameTaskFactory.scoreBoard(cardGame, () -> sendCardGameState(roomJoinCode))
//                ),
//                new Task<>(
//                        CardGameTaskInfo.GAME_FINISH,
//                        gameTaskFactory.done(room, cardGame, () -> sendCardGameResult(roomJoinCode))
//                )
//        ));
//    }

    public void selectCard(String joinCode, String playerName, Integer cardIndex) {
        final JoinCode roomJoinCode = new JoinCode(joinCode);
        final CardGame cardGame = getCardGame(roomJoinCode);
        final Player player = cardGame.findPlayerByName(new PlayerName(playerName));
        cardGame.selectCard(player, cardIndex);
        sendCardGameState(roomJoinCode);
        if (cardGame.isFinishedThisRound()) {
            cardGameTaskExecutors.cancelPlaying(roomJoinCode, CardGameTaskType.from(cardGame));
        }
    }

    private void sendCardGameState(JoinCode joinCode) {
        final CardGame cardGame = getCardGame(joinCode);
        final MiniGameStateMessage message = MiniGameStateMessage.from(cardGame);
        final String destination = String.format(CARD_GAME_STATE_DESTINATION_FORMAT, joinCode.value());
        messagingTemplate.convertAndSend(destination, WebSocketResponse.success(message));
    }

    private void sendCardGameResult(JoinCode joinCode) {
        final CardGame cardGame = getCardGame(joinCode);
        final String scoreDestination = String.format(CARD_GAME_SCORE_DESTINATION_FORMAT, joinCode.value());
        final String rankDestination = String.format(CARD_GAME_RESULT_DESTINATION_FORMAT, joinCode.value());
        messagingTemplate.convertAndSend(String.format(scoreDestination, joinCode.value()),
                WebSocketResponse.success(MiniGameScoresMessage.from(cardGame.calculateScores())));
        messagingTemplate.convertAndSend(String.format(rankDestination, joinCode.value()),
                WebSocketResponse.success(MiniGameRanksMessage.from(cardGame.getResult())));
    }

    private void sendGameStartMessage(String joinCode, MiniGameType miniGameType) {
        messagingTemplate.convertAndSend(
                String.format(GAME_START_DESTINATION_FORMAT, joinCode),
                WebSocketResponse.success(new MinIGameStartMessage(miniGameType))
        );
    }

    private CardGame getCardGame(JoinCode joinCode) {
        final Room room = roomQueryService.findByJoinCode(joinCode);
        return (CardGame) room.findMiniGame(MiniGameType.CARD_GAME);
    }
}
