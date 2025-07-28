package coffeeshout.minigame.application;

import coffeeshout.minigame.domain.cardgame.CardGameQueryService;
import coffeeshout.minigame.domain.cardgame.CardGame;
import coffeeshout.minigame.domain.cardgame.CardGameTaskExecutors;
import coffeeshout.minigame.domain.cardgame.card.CardGameDeckGenerator;
import coffeeshout.minigame.domain.cardgame.card.CardGameRandomDeckGenerator;
import coffeeshout.minigame.domain.temp.CardGameTaskFactory;
import coffeeshout.minigame.domain.temp.CardGameTaskInfo;
import coffeeshout.minigame.domain.temp.TaskExecutor;
import coffeeshout.minigame.domain.temp.TaskExecutor.Task;
import coffeeshout.minigame.ui.MiniGameStateMessage;
import coffeeshout.player.domain.Player;
import coffeeshout.room.domain.JoinCode;
import coffeeshout.room.domain.Room;
import coffeeshout.room.domain.RoomFinder;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CardGameService {

    private static final int ADDITION_CARD_COUNT = 6;
    private static final int MULTIPLIER_CARD_COUNT = 3;

    private static final String CARD_GAME_STATE_DESTINATION_FORMAT = "/topic/room/%s/gameState";
    private static final String CARD_GAME_RESULT_DESTINATION_FORMAT = "/topic/room/%s/rank";

    private final CardGameQueryService cardGameQueryService;
    private final RoomFinder roomFinder;
    private final CardGameRepository cardGameRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final CardGameTaskExecutors cardGameTaskExecutors;

    public void startGame(JoinCode joinCode) {
        final Room room = roomFinder.findByJoinCode(joinCode);
        final CardGameDeckGenerator deckGenerator = new CardGameRandomDeckGenerator();
        final CardGame cardGame = new CardGame(
                deckGenerator.generate(ADDITION_CARD_COUNT, MULTIPLIER_CARD_COUNT),
                room.getPlayers()
        );
        cardGameRepository.save(joinCode, cardGame);
        TaskExecutor<CardGameTaskInfo> executor = new TaskExecutor<>();
        cardGameTaskExecutors.put(joinCode, executor);
        executor.submits(List.of(
                new Task<>(CardGameTaskInfo.WAITING_FOR_START, CardGameTaskFactory.loading(cardGame, () -> sendCardGameState(joinCode))),
                new Task<>(CardGameTaskInfo.FIRST_ROUND_PLAYING, CardGameTaskFactory.play(cardGame, () -> sendCardGameState(joinCode))),
                new Task<>(CardGameTaskInfo.FIRST_ROUND_SCORE_BOARD, CardGameTaskFactory.scoreBoard(cardGame, () -> sendCardGameState(joinCode))),
                new Task<>(CardGameTaskInfo.FIRST_ROUND_LOADING,CardGameTaskFactory.loading(cardGame, () -> sendCardGameState(joinCode))),
                new Task<>(CardGameTaskInfo.SECOND_ROUND_PLAYING,CardGameTaskFactory.play(cardGame, () -> sendCardGameState(joinCode))),
                new Task<>(CardGameTaskInfo.SECOND_ROUND_SCORE_BOARD,CardGameTaskFactory.scoreBoard(cardGame, () -> sendCardGameState(joinCode))),
                new Task<>(CardGameTaskInfo.GAME_FINISH,CardGameTaskFactory.done(cardGame, () -> sendCardGameResult(joinCode)))
        ));
    }

    public void selectCard(JoinCode joinCode, String playerName, Integer cardIndex) {
        final CardGame cardGame = cardGameQueryService.getCardGame(joinCode);
        final Player player = cardGame.findPlayerByName(playerName);
        cardGame.selectCard(player, cardIndex);
        if (cardGame.isFinishedThisRound()) {
            cardGameTaskExecutors.cancelPlaying(joinCode, cardGame.getRound());
        }
    }

    private void sendCardGameState(JoinCode joinCode) {
        CardGame cardGame = cardGameQueryService.getCardGame(joinCode);
        MiniGameStateMessage message = MiniGameStateMessage.from(cardGame);
        String destination = String.format(CARD_GAME_STATE_DESTINATION_FORMAT, joinCode.getValue());
        messagingTemplate.convertAndSend(destination, message);
    }

    private void sendCardGameResult(JoinCode joinCode) {
        CardGame cardGame = cardGameQueryService.getCardGame(joinCode);
        String destination = String.format(CARD_GAME_RESULT_DESTINATION_FORMAT, joinCode.getValue());
        messagingTemplate.convertAndSend(destination, cardGame.getResult());
    }
}
