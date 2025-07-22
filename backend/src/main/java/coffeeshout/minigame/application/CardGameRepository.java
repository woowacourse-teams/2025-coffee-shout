package coffeeshout.minigame.application;

import coffeeshout.minigame.domain.cardgame.CardGame;
import java.util.Optional;

public interface CardGameRepository {

    Optional<CardGame> findByRoomId(Long roomId);

    CardGame save(Long roomId, CardGame cardGame);

    void delete(Long roomId);
}
