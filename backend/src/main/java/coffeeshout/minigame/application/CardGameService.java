package coffeeshout.minigame.application;

import coffeeshout.minigame.domain.MiniGameResult;
import coffeeshout.minigame.domain.cardgame.CardGame;
import coffeeshout.minigame.domain.cardgame.CardGameState;
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

    private static final String CARD_GAME_STATE_DESTINATION_FORMAT = "/topic/room/%d/gameState";
    private static final String CARD_GAME_RESULT_DESTINATION_FORMAT = "/topic/room/%d/gameState";

    private final RoomFinder roomFinder;
    private final CardGameRepository cardGameRepository;
    private final RoomTimers roomTimers;
    private final SimpMessagingTemplate messagingTemplate;

    public void startGame(Long roomId) {
        final Room room = roomFinder.findById(roomId);
        final CardGame cardGame = new CardGame(
                new CardGameRandomDeckGenerator(),
                room.getPlayers()
        );
        cardGameRepository.save(roomId, cardGame);
        startRound(roomId);
    }

    public void selectCard(Long roomId, String playerName, Integer cardIndex) {
        final CardGame cardGame = getCardGame(roomId);
        final Player player = cardGame.findPlayerByName(playerName);
        cardGame.selectCard(player, cardIndex);
        sendCardGameState(cardGame, roomId);

        if (cardGame.isRoundFinished()) {
            changeScoreBoard(roomId, cardGame);
        }
    }

    public MiniGameResult getMiniGameResult(Long roomId) {
        final CardGame cardGame = getCardGame(roomId);
        return cardGame.getResult();
    }

    public CardGame getCardGame(Long roomId) {
        return cardGameRepository.findByRoomId(roomId)
                .orElseThrow(() -> new NoSuchElementException("해당 룸에는 카드게임이 존재하지 않습니다."));
    }

    private void startRound(Long roomId) {
        final CardGame cardGame = getCardGame(roomId);
        cardGame.startRound();
        sendCardGameState(cardGame, roomId);
        roomTimers.start(roomId, () -> roundTimeout(cardGame, roomId), cardGame.getState().getDuration());
    }

    private void changeScoreBoard(Long roomId, CardGame cardGame) {
        roomTimers.cancel(roomId);
        cardGame.changeState(CardGameState.SCORE_BOARD);
        sendCardGameState(cardGame, roomId);
        sendCardGameResult(cardGame, roomId);
        roomTimers.start(
                roomId, () -> {
                    if (cardGame.isSecondRound()) {
                        // TODO 룸으로 이벤트 발행
                        return;
                    }
                    changeLoading(roomId, cardGame);
                }, cardGame.getState().getDuration()
        );
    }

    private void changeLoading(Long roomId, CardGame cardGame) {
        cardGame.changeState(CardGameState.LOADING);
        sendCardGameState(cardGame, roomId);
        roomTimers.start(roomId, () -> startRound(roomId), cardGame.getState().getDuration());
    }

    private void roundTimeout(CardGame cardGame, Long roomId) {
        roomTimers.cancel(roomId);
        cardGame.assignRandomCardsToUnselectedPlayers();
        sendCardGameState(cardGame, roomId);
        changeScoreBoard(roomId, cardGame);
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
}
