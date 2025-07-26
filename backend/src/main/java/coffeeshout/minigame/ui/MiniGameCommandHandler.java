package coffeeshout.minigame.ui;

public interface MiniGameCommandHandler<T extends MiniGameCommand> {

    void handle(String joinCode, T command);
    Class<T> getCommandType();
}
