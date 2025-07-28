package coffeeshout.minigame.ui.request;

import com.fasterxml.jackson.databind.JsonNode;

public record MiniGameMessage(
        CommandType commandType,
        JsonNode commandRequest
) {
}
