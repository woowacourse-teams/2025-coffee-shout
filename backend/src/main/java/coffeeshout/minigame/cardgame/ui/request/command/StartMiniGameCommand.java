package coffeeshout.minigame.cardgame.ui.request.command;

import coffeeshout.minigame.cardgame.ui.command.MiniGameCommand;

public record StartMiniGameCommand(String hostName) implements MiniGameCommand {
}
