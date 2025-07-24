package coffeeshout.minigame.application;

import coffeeshout.minigame.domain.cardgame.CardGame;
import coffeeshout.room.domain.JoinCode;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

@Repository
public class CardGameInMemoryRepository implements CardGameRepository {

    Map<JoinCode, CardGame> cardGameMap = new ConcurrentHashMap<>();

    @Override
    public Optional<CardGame> findByJoinCode(JoinCode joinCode) {
        return Optional.ofNullable(cardGameMap.get(joinCode));
    }

    @Override
    public CardGame save(JoinCode joinCode, CardGame cardGame) {
        return cardGameMap.put(joinCode, cardGame);
    }

    @Override
    public void delete(JoinCode joinCode) {
        cardGameMap.remove(joinCode);
    }
}
