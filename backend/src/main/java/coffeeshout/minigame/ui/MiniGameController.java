package coffeeshout.minigame.ui;

import coffeeshout.minigame.ui.command.MiniGameCommand;
import coffeeshout.minigame.ui.command.MiniGameCommandDispatcher;
import coffeeshout.minigame.ui.request.CommandType;
import coffeeshout.minigame.ui.request.MiniGameMessage;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class MiniGameController {

    private final MiniGameCommandDispatcher miniGameCommandDispatcher;
    private final ObjectMapper objectMapper;

    @MessageMapping("/room/{joinCode}/minigame/command")
    public void commandGame(@DestinationVariable String joinCode, @Payload MiniGameMessage command) {
        miniGameCommandDispatcher.dispatch(joinCode, map(command.commandType(), command.commandRequest()));
    }

    private MiniGameCommand map(CommandType commandType, JsonNode json) {
        return commandType.toCommandRequest(objectMapper, json);
    }
}
