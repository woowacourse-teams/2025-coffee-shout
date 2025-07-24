package coffeeshout.minigame.application;

import coffeeshout.minigame.domain.cardgame.CardGameQueryService;
import coffeeshout.minigame.domain.cardgame.CardGameTaskExecutor;
import coffeeshout.minigame.domain.cardgame.CardGameTaskExecutor.CardGameTask;
import coffeeshout.minigame.domain.cardgame.CardGame;
import coffeeshout.minigame.domain.cardgame.CardGameState;
import coffeeshout.minigame.domain.cardgame.CardGameTaskExecutors;
import coffeeshout.minigame.domain.cardgame.card.CardGameDeckGenerator;
import coffeeshout.minigame.domain.cardgame.card.CardGameRandomDeckGenerator;
import coffeeshout.minigame.ui.MiniGameStateMessage;
import coffeeshout.player.domain.Player;
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

    private static final String CARD_GAME_STATE_DESTINATION_FORMAT = "/topic/room/%d/gameState";
    private static final String CARD_GAME_RESULT_DESTINATION_FORMAT = "/topic/room/%d/rank";

    private final CardGameQueryService cardGameQueryService;
    private final RoomFinder roomFinder;
    private final CardGameRepository cardGameRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final CardGameTaskExecutors cardGameTaskExecutors;

    public void startGame(Long roomId) {
        final Room room = roomFinder.findById(roomId);
        final CardGameDeckGenerator deckGenerator = new CardGameRandomDeckGenerator();
        final CardGame cardGame = new CardGame(
                deckGenerator.generate(ADDITION_CARD_COUNT, MULTIPLIER_CARD_COUNT),
                room.getPlayers()
        );
        cardGameRepository.save(roomId, cardGame);
        CardGameTaskExecutor executor = new CardGameTaskExecutor(List.of(
                play(roomId), // 1라운드 시작
                scoreBoard(roomId), // 1라운드 결과
                loading(roomId), // 1라운드 끝나고 로딩
                play(roomId), // 2라운드 시작
                scoreBoard(roomId), // 2라운드 끝나고 결과
                done(roomId)
        ));
        cardGameTaskExecutors.put(roomId, executor);
        executor.submits();
    }

    public void selectCard(Long roomId, String playerName, Integer cardIndex) {
        final CardGame cardGame = cardGameQueryService.getCardGame(roomId);
        final Player player = cardGame.findPlayerByName(playerName);
        cardGame.selectCard(player, cardIndex);
        sendCardGameState(roomId);
        if (cardGame.isFinishedThisRound()) {
            cardGameTaskExecutors.get(roomId).cancelPlaying();
        }
    }

    private CardGameTask play(Long roomId) {
        CardGame cardGame = cardGameQueryService.getCardGame(roomId);
        return new CardGameTask(CardGameState.PLAYING, cardGame::startRound, () -> {
            cardGame.assignRandomCardsToUnselectedPlayers();
            postTask(roomId).run();
        });
    }

    private CardGameTask scoreBoard(Long roomId) {
        return new CardGameTask(CardGameState.SCORE_BOARD, () -> {
            CardGame cardGame = cardGameQueryService.getCardGame(roomId);
            cardGame.changeScoreBoardState();
        }, postTask(roomId));
    }

    private CardGameTask loading(Long roomId) {
        return new CardGameTask(CardGameState.LOADING, () -> {
            CardGame cardGame = cardGameQueryService.getCardGame(roomId);
            cardGame.changeLoadingState();
        }, postTask(roomId));
    }

    private CardGameTask done(Long roomId) {
        return new CardGameTask(CardGameState.DONE, () -> {
            CardGame cardGame = cardGameQueryService.getCardGame(roomId);
            sendCardGameResult(roomId);
            cardGame.changeDoneState();
        }, () -> {});
    }

    private Runnable postTask(Long roomId) {
        return () -> sendCardGameState(roomId);
    }

    private void sendCardGameState(Long roomId) {
        CardGame cardGame = cardGameQueryService.getCardGame(roomId);
        MiniGameStateMessage message = MiniGameStateMessage.from(cardGame);
        String destination = String.format(CARD_GAME_STATE_DESTINATION_FORMAT, roomId);
        messagingTemplate.convertAndSend(destination, message);
    }

    private void sendCardGameResult(Long roomId) {
        CardGame cardGame = cardGameQueryService.getCardGame(roomId);
        String destination = String.format(CARD_GAME_RESULT_DESTINATION_FORMAT, roomId);
        messagingTemplate.convertAndSend(destination, cardGame.getResult());
    }
}
