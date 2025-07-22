package coffeeshout.minigame.application;

import static org.springframework.util.Assert.state;

import coffeeshout.minigame.domain.cardgame.CardGameRound;
import coffeeshout.minigame.domain.cardgame.card.CardGameDeckGenerator;
import coffeeshout.minigame.ui.MiniGameRanksMessage;
import coffeeshout.minigame.ui.MiniGameStateMessage;
import coffeeshout.room.domain.RoomFinder;
import coffeeshout.minigame.domain.cardgame.CardGame;
import coffeeshout.minigame.domain.cardgame.card.CardGameRandomDeckGenerator;
import coffeeshout.minigame.domain.MiniGameResult;
import coffeeshout.player.domain.Player;
import coffeeshout.room.domain.Room;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CardGameService {

    private static final int ADDITION_CARD_COUNT = 6;
    private static final int MULTIPLIER_CARD_COUNT = 3;

    private final RoomFinder roomFinder;
    // 분리하는게 좋을듯
    // 토픽 브로커가 따로 있으면 좋을 것 같음
    private final Map<Long, CardGame> cardGames = new ConcurrentHashMap<>();
    private final Map<Long, ScheduledFuture<?>> timers = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(5);
    private final SimpMessagingTemplate messagingTemplate;

    public void start(Long roomId) {
        final Room room = roomFinder.findById(roomId);
        final CardGameDeckGenerator deckGenerator = new CardGameRandomDeckGenerator();
        final CardGame cardGame = new CardGame(deckGenerator.generate(ADDITION_CARD_COUNT, MULTIPLIER_CARD_COUNT), room.getPlayers());
        cardGame.start();
        timers.put(roomId, createTimer(cardGame, roomId));

        cardGames.put(roomId, cardGame);
    }

    public void selectCard(Long roomId, String playerName, Integer cardIndex) {
        final CardGame cardGame = cardGames.get(roomId);
        final Player player = cardGame.findPlayerByName(playerName);

        cardGame.selectCard(player, cardIndex);
    }
/*
Room을 찾아서 특정 메서드를 호출하면 기존에 생성되어 있는 Room이랑 동기화가 안된다.
 */
    public void checkAndMoveRound(Long roomId) {
        final CardGame cardGame = cardGames.get(roomId);

        if (cardGame.isFinished(CardGameRound.FIRST)) {
            state(cardGame.isFirstRound(), "게임이 1라운드가 아닙니다.");
            cardGame.nextRound();
            sendCardGameState(cardGame, roomId);
            cardGame.initGame();
            if(timers.containsKey(roomId)){
                timers.get(roomId).cancel(true);
                timers.put(roomId, createTimer(cardGame, roomId));
            }
        }

        if (cardGame.isFinished(CardGameRound.SECOND)) {
            state(cardGame.isSecondRound(), "게임이 2라운드가 아닙니다.");
            cardGame.nextRound();
            sendCardGameResult(cardGame, roomId);
            if(timers.containsKey(roomId)){
                timers.get(roomId).cancel(true);
                timers.remove(roomId);
            }
            // TODO: Room으로 결과 이벤트 전달
        }
    }

    public MiniGameResult getMiniGameResult(Long roomId) {
        final CardGame cardGame = cardGames.get(roomId);

        return cardGame.getResult();
    }

    public CardGame getCardGame(Long roomId) {
        return cardGames.get(roomId);
    }

    private void roundTimeout(CardGame cardGame, Long roomId) {
        cardGame.assignRandomCardsToUnselectedPlayers();
        sendCardGameState(cardGame, roomId);
        checkAndMoveRound(roomId);
    }

    private void sendCardGameState(CardGame cardGame, Long roomId) {
        messagingTemplate.convertAndSend("/topic/room/" + roomId + "/gameState", MiniGameStateMessage.from(cardGame));
    }

    private void sendCardGameResult(CardGame cardGame, Long roomId){
        messagingTemplate.convertAndSend("/topic/room/" + roomId + "/finish", MiniGameRanksMessage.from(cardGame.getResult()));
    }

    private ScheduledFuture<?> createTimer(CardGame cardGame, Long roomId){
        return scheduledExecutorService.schedule(() -> roundTimeout(cardGame, roomId), 10, java.util.concurrent.TimeUnit.SECONDS);
    }
}
