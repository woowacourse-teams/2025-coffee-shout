package coffeeshout.minigame.ui.request;

import coffeeshout.minigame.domain.MiniGameType;
import com.fasterxml.jackson.databind.JsonNode;

public record MiniGameMessage(
        MiniGameType miniGameType,
        CommandType commandType,
        JsonNode commandRequest
) {
}
