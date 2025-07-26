package coffeeshout.minigame.ui.handler;

import coffeeshout.minigame.domain.MiniGameType;
import coffeeshout.minigame.ui.MiniGameCommand;

public record SelectCardCommand(String playerName, Integer cardIndex) implements MiniGameCommand {

    @Override
    public MiniGameType getMiniGameType() {
        return MiniGameType.CARD_GAME;
    }
}
