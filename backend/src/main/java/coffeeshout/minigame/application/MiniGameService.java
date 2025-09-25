package coffeeshout.minigame.application;

public interface MiniGameService {

    void publishStartEvent(String joinCode, String hostName);

    void publishSelectCardEvent(String joinCode, String playerName, Integer cardIndex);
}
