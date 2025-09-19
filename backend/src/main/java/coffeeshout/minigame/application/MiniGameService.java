package coffeeshout.minigame.application;

import coffeeshout.room.domain.Playable;
import java.util.concurrent.CompletableFuture;

public interface MiniGameService {

    void start(Playable playable, String joinCode);
    
    CompletableFuture<Void> startAsync(Playable playable, String joinCode);
}
