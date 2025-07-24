package coffeeshout.room.domain.service;

import coffeeshout.minigame.domain.cardgame.CardGame;
import coffeeshout.minigame.domain.cardgame.CardGameRandomDeckGenerator;
import coffeeshout.room.domain.Playable;
import coffeeshout.room.domain.player.Player;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MiniGameQueryService {

    public Playable getCardGame(List<Player> players) {
        return new CardGame(players, new CardGameRandomDeckGenerator());

    }

}
