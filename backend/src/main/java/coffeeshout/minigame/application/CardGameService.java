package coffeeshout.minigame.application;

import coffeeshout.minigame.domain.MiniGameType;
import coffeeshout.minigame.domain.cardgame.CardGame;
import coffeeshout.minigame.domain.cardgame.CardGameTaskExecutors;
import coffeeshout.minigame.domain.temp.CardGameTaskFactory;
import coffeeshout.minigame.domain.temp.CardGameTaskInfo;
import coffeeshout.minigame.domain.temp.TaskExecutor;
import coffeeshout.minigame.domain.temp.TaskExecutor.Task;
import coffeeshout.minigame.ui.response.MiniGameStateMessage;
import coffeeshout.room.domain.JoinCode;
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
public class CardGameService {

    private static final String CARD_GAME_STATE_DESTINATION_FORMAT = "/topic/room/%s/gameState";
    private static final String CARD_GAME_RESULT_DESTINATION_FORMAT = "/topic/room/%s/rank";

    private final RoomQueryService roomQueryService;
    private final SimpMessagingTemplate messagingTemplate;
    private final CardGameTaskExecutors cardGameTaskExecutors;

    public void startGame(String joinCode) {
        final JoinCode roomJoinCode = new JoinCode(joinCode);
        final Room room = roomQueryService.findByJoinCode(roomJoinCode);
        room.startGame(MiniGameType.CARD_GAME);
        CardGame cardGame = (CardGame) room.findMiniGame(MiniGameType.CARD_GAME);
        TaskExecutor<CardGameTaskInfo> executor = new TaskExecutor<>();
        cardGameTaskExecutors.put(roomJoinCode, executor);
        executor.submits(List.of(
                new Task<>(
                        CardGameTaskInfo.WAITING_FOR_START,
                        CardGameTaskFactory.loading(cardGame, () -> sendCardGameState(joinCode))
                ),
                new Task<>(
                        CardGameTaskInfo.FIRST_ROUND_PLAYING,
                        CardGameTaskFactory.play(cardGame, () -> sendCardGameState(joinCode))
                ),
                new Task<>(
                        CardGameTaskInfo.FIRST_ROUND_SCORE_BOARD,
                        CardGameTaskFactory.scoreBoard(cardGame, () -> sendCardGameState(joinCode))
                ),
                new Task<>(
                        CardGameTaskInfo.FIRST_ROUND_LOADING,
                        CardGameTaskFactory.loading(cardGame, () -> sendCardGameState(joinCode))
                ),
                new Task<>(
                        CardGameTaskInfo.SECOND_ROUND_PLAYING,
                        CardGameTaskFactory.play(cardGame, () -> sendCardGameState(joinCode))
                ),
                new Task<>(
                        CardGameTaskInfo.SECOND_ROUND_SCORE_BOARD,
                        CardGameTaskFactory.scoreBoard(cardGame, () -> sendCardGameState(joinCode))
                ),
                new Task<>(
                        CardGameTaskInfo.GAME_FINISH,
                        CardGameTaskFactory.done(room, cardGame, () -> sendCardGameResult(joinCode))
                )
        ));
    }

    public void selectCard(String joinCode, String playerName, Integer cardIndex) {
        JoinCode roomJoinCode = new JoinCode(joinCode);
        final CardGame cardGame = getCardGame(roomJoinCode);
        final Player player = cardGame.findPlayerByName(new PlayerName(playerName));
        cardGame.selectCard(player, cardIndex);
        if (cardGame.isFinishedThisRound()) {
            cardGameTaskExecutors.cancelPlaying(roomJoinCode, cardGame.getRound());
        }
    }

    private void sendCardGameState(String joinCode) {
        CardGame cardGame = getCardGame(new JoinCode(joinCode));
        MiniGameStateMessage message = MiniGameStateMessage.from(cardGame);
        String destination = String.format(CARD_GAME_STATE_DESTINATION_FORMAT, joinCode);
        messagingTemplate.convertAndSend(destination, message);
    }

    private void sendCardGameResult(String joinCode) {
        CardGame cardGame = getCardGame(new JoinCode(joinCode));
        String destination = String.format(CARD_GAME_RESULT_DESTINATION_FORMAT, joinCode);
        messagingTemplate.convertAndSend(destination, cardGame.getResult());
    }

    private CardGame getCardGame(JoinCode joinCode) {
        Room room = roomQueryService.findByJoinCode(joinCode);
        return (CardGame) room.findMiniGame(MiniGameType.CARD_GAME);
    }
}
