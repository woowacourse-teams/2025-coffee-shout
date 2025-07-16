package coffeeshout.application;

import coffeeshout.domain.CardGame;
import coffeeshout.domain.MiniGameResult;
import coffeeshout.domain.Room;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CardGameService {

    private final RoomQueryService roomQueryService;

    private final PlayerQueryService playerQueryService;

    private final Map<Long, CardGame> cardGames = new ConcurrentHashMap<>();

    public void start(Long roomId) {
        final Room room = roomQueryService.findById(roomId);
        final CardGame cardGame = new CardGame(room.getPlayers());

        cardGames.put(roomId, cardGame);
    }

    public void selectCard(Long roomId, Long playerId, Integer cardPosition) {
        final CardGame cardGame = cardGames.get(roomId);

        cardGame.selectCard(playerQueryService.findById(playerId), cardPosition);
    }

    public void checkRound(Long roomId) {
        final CardGame cardGame = cardGames.get(roomId);

        if (cardGame.isFirstRoundFinished()) {
            cardGame.nextRound();
            cardGame.shuffle();
        }

        if(cardGame.isSecondRoundFinished()) {
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
