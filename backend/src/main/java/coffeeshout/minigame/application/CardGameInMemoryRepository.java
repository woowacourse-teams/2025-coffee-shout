package coffeeshout.minigame.application;

import coffeeshout.minigame.domain.cardgame.CardGame;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

@Repository
public class CardGameInMemoryRepository implements CardGameRepository {

    Map<Long, CardGame> cardGameMap = new ConcurrentHashMap<>();

    @Override
    public Optional<CardGame> findByRoomId(Long roomId) {
        return Optional.ofNullable(cardGameMap.get(roomId));
    }

    @Override
    public CardGame save(Long roomId, CardGame cardGame) {
        return cardGameMap.put(roomId, cardGame);
    }

    @Override
    public void delete(Long roomId) {
        cardGameMap.remove(roomId);
    }
}
