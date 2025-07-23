package coffeeshout.minigame.application;

import coffeeshout.minigame.application.RoomTaskExecutor.RoomTask;
import coffeeshout.minigame.domain.cardgame.CardGame;
import coffeeshout.minigame.domain.cardgame.CardGameState;
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
    private final RoomTaskExecutors roomTaskExecutors = new RoomTaskExecutors();

    public void startGame(Long roomId) {
        final Room room = roomFinder.findById(roomId);
        final CardGameDeckGenerator deckGenerator = new CardGameRandomDeckGenerator();
        final CardGame cardGame = new CardGame(
                deckGenerator.generate(ADDITION_CARD_COUNT, MULTIPLIER_CARD_COUNT),
                room.getPlayers()
        );
        cardGameRepository.save(roomId, cardGame);
        RoomTaskExecutor executor = new RoomTaskExecutor(List.of(
                play(roomId), // 1라운드 시작
                scoreBoard(roomId), // 1라운드 결과
                loading(roomId), // 1라운드 끝나고 로딩
                play(roomId), // 2라운드 시작
                scoreBoard(roomId), // 2라운드 끝나고 결과
                done(roomId)
        ));
        roomTaskExecutors.put(roomId, executor);
        executor.submits();
    }

    public void selectCard(Long roomId, String playerName, Integer cardIndex) {
        final CardGame cardGame = cardGameQueryService.getCardGame(roomId);
        final Player player = cardGame.findPlayerByName(playerName);
        cardGame.selectCard(player, cardIndex);
        sendCardGameState(roomId);
        if (cardGame.isFinishedThisRound()) {
            roomTaskExecutors.get(roomId).cancelPlaying();
        }
    }

    private RoomTask play(Long roomId) {
        CardGame cardGame = cardGameQueryService.getCardGame(roomId);
        return new RoomTask(CardGameState.PLAYING, cardGame::startRound, () -> {
            postTask(roomId);
            cardGame.assignRandomCardsToUnselectedPlayers();
        });
    }

    private RoomTask scoreBoard(Long roomId) {
        return new RoomTask(CardGameState.SCORE_BOARD, () -> {
            CardGame cardGame = cardGameQueryService.getCardGame(roomId);
            cardGame.changeScoreBoardState();
        }, postTask(roomId));
    }

    private RoomTask loading(Long roomId) {
        return new RoomTask(CardGameState.LOADING, () -> {
            CardGame cardGame = cardGameQueryService.getCardGame(roomId);
            cardGame.changeLoadingState();
        }, postTask(roomId));
    }

    private RoomTask done(Long roomId) {
        return new RoomTask(CardGameState.DONE, () -> sendCardGameResult(roomId), () -> {});
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
