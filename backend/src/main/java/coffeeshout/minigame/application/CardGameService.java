package coffeeshout.minigame.application;

import static org.springframework.util.Assert.state;

import coffeeshout.minigame.domain.cardgame.CardGameRound;
import coffeeshout.minigame.domain.cardgame.card.CardGameDeckGenerator;
import coffeeshout.room.domain.RoomFinder;
import coffeeshout.minigame.domain.cardgame.CardGame;
import coffeeshout.minigame.domain.cardgame.card.CardGameRandomDeckGenerator;
import coffeeshout.minigame.domain.MiniGameResult;
import coffeeshout.player.domain.Player;
import coffeeshout.room.domain.Room;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CardGameService {

    private static final int ADDITION_CARD_COUNT = 6;
    private static final int MULTIPLIER_CARD_COUNT = 3;

    private final RoomFinder roomFinder;
    private final Map<Long, CardGame> cardGames = new ConcurrentHashMap<>();


    public void start(Long roomId) {
        final Room room = roomFinder.findById(roomId);
        final CardGameDeckGenerator deckGenerator = new CardGameRandomDeckGenerator();
        final CardGame cardGame = new CardGame(deckGenerator.generate(ADDITION_CARD_COUNT, MULTIPLIER_CARD_COUNT), room.getPlayers());
        cardGame.start();
        cardGames.put(roomId, cardGame);
    }

    public void selectCard(Long roomId, String playerName, Integer cardIndex) {
        final CardGame cardGame = cardGames.get(roomId);
        final Player player = cardGame.findPlayerByName(playerName);

        cardGame.selectCard(player, cardIndex);
    }

    public void checkAndMoveRound(Long roomId) {
        final CardGame cardGame = cardGames.get(roomId);

        if (cardGame.isFinished(CardGameRound.FIRST)) {
            state(cardGame.isFirstRound(), "게임이 1라운드가 아닙니다.");
            cardGame.nextRound();
            cardGame.initGame();
        }

        if (cardGame.isFinished(CardGameRound.SECOND)) {
            state(cardGame.isSecondRound(), "게임이 2라운드가 아닙니다.");
            cardGame.nextRound();
        }
    }

    public MiniGameResult getMiniGameResult(Long roomId) {
        final CardGame cardGame = cardGames.get(roomId);

        return cardGame.getResult();
    }

    public CardGame getCardGame(Long roomId) {
        return cardGames.get(roomId);
    }
}
