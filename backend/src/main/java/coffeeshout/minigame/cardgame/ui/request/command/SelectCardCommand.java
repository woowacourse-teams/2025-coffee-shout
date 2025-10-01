package coffeeshout.minigame.cardgame.ui.request.command;

import coffeeshout.minigame.cardgame.ui.command.MiniGameCommand;

public record SelectCardCommand(String playerName, Integer cardIndex) implements MiniGameCommand {
}
