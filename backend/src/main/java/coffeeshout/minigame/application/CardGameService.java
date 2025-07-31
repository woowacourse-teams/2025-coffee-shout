package coffeeshout.minigame.application;

import coffeeshout.global.ui.WebSocketResponse;
import coffeeshout.minigame.domain.MiniGameType;
import coffeeshout.minigame.domain.cardgame.CardGame;
import coffeeshout.minigame.domain.cardgame.CardGameTaskExecutors;
import coffeeshout.minigame.domain.temp.CardGameTaskFactory;
import coffeeshout.minigame.domain.temp.CardGameTaskInfo;
import coffeeshout.minigame.domain.temp.TaskExecutor;
import coffeeshout.minigame.domain.temp.TaskExecutor.Task;
import coffeeshout.minigame.ui.response.MiniGameRanksMessage;
import coffeeshout.minigame.ui.response.MiniGameScoresMessage;
import coffeeshout.minigame.ui.response.MiniGameStartMessage;
import coffeeshout.minigame.ui.response.MiniGameStateMessage;
import coffeeshout.room.domain.JoinCode;
import coffeeshout.room.domain.Playable;
import coffeeshout.room.domain.Room;
import coffeeshout.room.domain.player.Player;
import coffeeshout.room.domain.player.PlayerName;
import coffeeshout.room.domain.service.RoomQueryService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CardGameService implements MiniGameService {

    private static final String CARD_GAME_STATE_DESTINATION_FORMAT = "/topic/room/%s/gameState";
    private static final String CARD_GAME_SCORE_DESTINATION_FORMAT = "/topic/room/%s/score";
    private static final String CARD_GAME_RESULT_DESTINATION_FORMAT = "/topic/room/%s/rank";
    private static final String GAME_START_DESTINATION_FORMAT = "/topic/room/%s/round";

    private final RoomQueryService roomQueryService;
    private final SimpMessagingTemplate messagingTemplate;
    private final CardGameTaskExecutors cardGameTaskExecutors;
    private final CardGameTaskFactory gameTaskFactory;

    @Override
    public void start(Playable playable, String joinCode) {
        sendGameStartMessage(joinCode, playable.getMiniGameType());
        final JoinCode roomJoinCode = new JoinCode(joinCode);
        final Room room = roomQueryService.findByJoinCode(roomJoinCode);
        final CardGame cardGame = (CardGame) playable;
        final TaskExecutor<CardGameTaskInfo> executor = new TaskExecutor<>();
        cardGameTaskExecutors.put(roomJoinCode, executor);
        executor.submits(List.of(
                new Task<>(
                        CardGameTaskInfo.FIRST_ROUND_LOADING,
                        gameTaskFactory.loading(cardGame, () -> sendCardGameState(roomJoinCode))
                ),
                new Task<>(
                        CardGameTaskInfo.FIRST_ROUND_PLAYING,
                        gameTaskFactory.play(cardGame, () -> sendCardGameState(roomJoinCode))
                ),
                new Task<>(
                        CardGameTaskInfo.FIRST_ROUND_SCORE_BOARD,
                        gameTaskFactory.scoreBoard(cardGame, () -> sendCardGameState(roomJoinCode))
                ),
                new Task<>(
                        CardGameTaskInfo.SECOND_ROUND_LOADING,
                        gameTaskFactory.loading(cardGame, () -> sendCardGameState(roomJoinCode))
                ),
                new Task<>(
                        CardGameTaskInfo.SECOND_ROUND_PLAYING,
                        gameTaskFactory.play(cardGame, () -> sendCardGameState(roomJoinCode))
                ),
                new Task<>(
                        CardGameTaskInfo.SECOND_ROUND_SCORE_BOARD,
                        gameTaskFactory.scoreBoard(cardGame, () -> sendCardGameState(roomJoinCode))
                ),
                new Task<>(
                        CardGameTaskInfo.GAME_FINISH,
                        gameTaskFactory.done(room, cardGame, () -> {
                            sendCardGameState(roomJoinCode);
                            sendCardGameResult(roomJoinCode);
                        })
                )
        ));
    }

    public void selectCard(String joinCode, String playerName, Integer cardIndex) {
        final JoinCode roomJoinCode = new JoinCode(joinCode);
        final CardGame cardGame = getCardGame(roomJoinCode);
        final Player player = cardGame.findPlayerByName(new PlayerName(playerName));
        cardGame.selectCard(player, cardIndex);
        sendCardGameState(roomJoinCode);
        if (cardGame.isFinishedThisRound()) {
            cardGameTaskExecutors.cancelPlaying(roomJoinCode, cardGame.getRound());
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
                WebSocketResponse.success(new MiniGameStartMessage(miniGameType))
        );
    }

    private CardGame getCardGame(JoinCode joinCode) {
        final Room room = roomQueryService.findByJoinCode(joinCode);
        return (CardGame) room.findMiniGame(MiniGameType.CARD_GAME);
    }
}
