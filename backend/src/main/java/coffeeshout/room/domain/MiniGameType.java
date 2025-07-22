package coffeeshout.room.domain;

import coffeeshout.minigame.domain.cardgame.CardGame;
import coffeeshout.minigame.domain.cardgame.CardGameRandomDeckGenerator;
import coffeeshout.room.domain.player.Player;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum MiniGameType {

    CARD_GAME,
    ;

    public Playable createMiniGame(List<Player> players) {
        switch (this) {
            case CARD_GAME:
                return new CardGame(players, new CardGameRandomDeckGenerator());
        }

        throw new IllegalArgumentException("선택한 게임이 존재하지 않습니다.");
    }
}
