package coffeeshout.minigame.domain;

import coffeeshout.minigame.domain.cardgame.CardGame;
import coffeeshout.minigame.domain.cardgame.card.CardGameRandomDeckGenerator;
import coffeeshout.room.domain.Playable;
import generator.annotaions.WebSocketMessage;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@WebSocketMessage
public enum MiniGameType {

    CARD_GAME,
    ;

    public Playable createMiniGame() {
        switch (this) {
            case CARD_GAME:
                return new CardGame(new CardGameRandomDeckGenerator());
        }

        throw new IllegalArgumentException("선택한 게임이 존재하지 않습니다.");
    }
}
