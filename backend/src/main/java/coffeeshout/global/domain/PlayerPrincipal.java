package coffeeshout.global.domain;

import java.security.Principal;
import lombok.Getter;

@Getter
public class PlayerPrincipal implements Principal {

    private final String sessionId;
    private final String playerName;
    private final String joinCode;

    public PlayerPrincipal(String sessionId, String playerName, String joinCode) {
        this.sessionId = sessionId;
        this.playerName = playerName;
        this.joinCode = joinCode;
    }

    @Override
    public String getName() {
        return String.format("%s:%s", joinCode, playerName);
    }
}
