package coffeeshout.minigame.application;

import static org.springframework.util.Assert.state;

import coffeeshout.room.domain.service.RoomFinder;
import coffeeshout.minigame.domain.cardgame.CardGame;
import coffeeshout.minigame.domain.cardgame.CardGameRandomDeckGenerator;
import coffeeshout.minigame.domain.MiniGameResult;
import coffeeshout.player.domain.Player;
import coffeeshout.room.domain.RouletteRoom;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CardGameService {

    private final RoomFinder roomFinder;
    private final Map<Long, CardGame> cardGames = new ConcurrentHashMap<>();

    public void start(Long roomId) {
        final RouletteRoom room = roomFinder.findById(roomId);
        final CardGame cardGame = new CardGame(room.getPlayers(), new CardGameRandomDeckGenerator());

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

        if (cardGame.isFirstRoundFinished()) {
            state(cardGame.isFirstRound(), "게임이 1라운드가 아닙니다.");
            cardGame.nextRound();
            cardGame.shuffle();
        }

        if (cardGame.isSecondRoundFinished()) {
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
