package coffeeshout.minigame.ui.request.command;

import coffeeshout.minigame.domain.MiniGameType;
import coffeeshout.minigame.ui.command.MiniGameCommand;

public record SelectCardCommand(String playerName, Integer cardIndex) implements MiniGameCommand {

    @Override
    public MiniGameType getMiniGameType() {
        return MiniGameType.CARD_GAME;
    }
}
