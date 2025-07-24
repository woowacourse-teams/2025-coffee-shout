package coffeeshout.minigame.application;

import coffeeshout.minigame.domain.cardgame.CardGame;
import coffeeshout.room.domain.JoinCode;
import java.util.Optional;

public interface CardGameRepository {

    Optional<CardGame> findByJoinCode(JoinCode joinCode);

    CardGame save(JoinCode joinCode, CardGame cardGame);

    void delete(JoinCode joinCode);
}
