package coffeeshout.minigame;

public interface MiniGameService {

    void start(String joinCode, String hostName);

    MiniGameType getMiniGameType();
}
