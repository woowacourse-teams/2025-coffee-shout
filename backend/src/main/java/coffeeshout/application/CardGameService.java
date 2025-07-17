package coffeeshout.application;

import static org.springframework.util.Assert.state;

import coffeeshout.domain.CardGame;
import coffeeshout.domain.CardGameRandomDeckGenerator;
import coffeeshout.domain.MiniGameResult;
import coffeeshout.domain.Player;
import coffeeshout.domain.Room;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CardGameService {

    private final RoomQueryService roomQueryService;
    private final Map<Long, CardGame> cardGames = new ConcurrentHashMap<>();

    public void start(Long roomId) {
        final Room room = roomQueryService.findById(roomId);
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
