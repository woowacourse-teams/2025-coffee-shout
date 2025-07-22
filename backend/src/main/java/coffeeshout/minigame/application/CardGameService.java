package coffeeshout.minigame.application;

import coffeeshout.minigame.domain.MiniGameResult;
import coffeeshout.minigame.domain.cardgame.CardGame;
import coffeeshout.minigame.domain.cardgame.card.CardGameDeckGenerator;
import coffeeshout.minigame.domain.cardgame.card.CardGameRandomDeckGenerator;
import coffeeshout.minigame.ui.MiniGameStateMessage;
import coffeeshout.player.domain.Player;
import coffeeshout.room.domain.Room;
import coffeeshout.room.domain.RoomFinder;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CardGameService {

    private static final int ADDITION_CARD_COUNT = 6;
    private static final int MULTIPLIER_CARD_COUNT = 3;

    private static final String CARD_GAME_STATE_DESTINATION_FORMAT = "/topic/room/%d/gameState";
    private static final String CARD_GAME_RESULT_DESTINATION_FORMAT = "/topic/room/%d/gameState";

    private final RoomFinder roomFinder;
    private final CardGameRepository cardGameRepository;
    private final RoomTimers roomTimers;
    private final SimpMessagingTemplate messagingTemplate;

    public void start(Long roomId) {
        final Room room = roomFinder.findById(roomId);
        final CardGameDeckGenerator deckGenerator = new CardGameRandomDeckGenerator();
        final CardGame cardGame = new CardGame(
                deckGenerator.generate(ADDITION_CARD_COUNT, MULTIPLIER_CARD_COUNT),
                room.getPlayers()
        );
        cardGame.start();
        startRoundTimer(roomId, cardGame);
        cardGameRepository.save(roomId, cardGame);
    }

    public void selectCard(Long roomId, String playerName, Integer cardIndex) {
        final CardGame cardGame = cardGameRepository.findByRoomId(roomId)
                .orElseThrow(() -> new NoSuchElementException("해당 룸에는 카드게임이 존재하지 않습니다."));
        final Player player = cardGame.findPlayerByName(playerName);
        cardGame.selectCard(player, cardIndex);
    }

    public void checkAndMoveRound(Long roomId) {
//        final CardGame cardGame = cardGameRepository.findByRoomId(roomId)
//                .orElseThrow(() -> new NoSuchElementException("해당 룸에는 카드게임이 존재하지 않습니다."));
//
//        if (cardGame.isFinished(CardGameRound.FIRST)) {
//            state(cardGame.isFirstRound(), "게임이 1라운드가 아닙니다.");
//            cardGame.nextRound();
//            sendCardGameState(cardGame, roomId);
//            cardGame.initGame();
//            if (timers.containsKey(roomId)) {
//                timers.get(roomId).cancel(true);
//                timers.put(roomId, createTimer(cardGame, roomId));
//            }
//        }
//
//        if (cardGame.isFinished(CardGameRound.SECOND)) {
//            state(cardGame.isSecondRound(), "게임이 2라운드가 아닙니다.");
//            cardGame.nextRound();
//            sendCardGameResult(cardGame, roomId);
//            if (timers.containsKey(roomId)) {
//                timers.get(roomId).cancel(true);
//                timers.remove(roomId);
//            }
//            // TODO: Room으로 결과 이벤트 전달
//        }
    }

    public MiniGameResult getMiniGameResult(Long roomId) {
        final CardGame cardGame = cardGameRepository.findByRoomId(roomId)
                .orElseThrow(() -> new NoSuchElementException("해당 룸에는 카드게임이 존재하지 않습니다."));
        return cardGame.getResult();
    }

    public CardGame getCardGame(Long roomId) {
        return cardGameRepository.findByRoomId(roomId)
                .orElseThrow(() -> new NoSuchElementException("해당 룸에는 카드게임이 존재하지 않습니다."));
    }

    private void roundTimeout(CardGame cardGame, Long roomId) {
        cardGame.assignRandomCardsToUnselectedPlayers();
        sendCardGameState(cardGame, roomId);
        checkAndMoveRound(roomId);
    }

    private void sendCardGameState(CardGame cardGame, Long roomId) {
        MiniGameStateMessage message = MiniGameStateMessage.from(cardGame);
        String destination = String.format(CARD_GAME_STATE_DESTINATION_FORMAT, roomId);
        messagingTemplate.convertAndSend(destination, message);
    }

    private void sendCardGameResult(CardGame cardGame, Long roomId) {
        MiniGameStateMessage message = MiniGameStateMessage.from(cardGame);
        String destination = String.format(CARD_GAME_RESULT_DESTINATION_FORMAT, roomId);
        messagingTemplate.convertAndSend(destination, message);
    }

    private void startRoundTimer(Long roomId, CardGame cardGame) {
        roomTimers.start(roomId, () -> roundTimeout(cardGame, roomId), 10000);
    }
}
