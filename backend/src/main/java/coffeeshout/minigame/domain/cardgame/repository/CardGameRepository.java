package coffeeshout.minigame.domain.cardgame.repository;

import coffeeshout.minigame.domain.cardgame.CardGame;
import coffeeshout.room.domain.JoinCode;
import java.util.Optional;

public interface CardGameRepository {

    Optional<CardGame> findByJoinCode(JoinCode joinCode);

    CardGame save(CardGame cardGame);

    void deleteByJoinCode(JoinCode joinCode);
}
