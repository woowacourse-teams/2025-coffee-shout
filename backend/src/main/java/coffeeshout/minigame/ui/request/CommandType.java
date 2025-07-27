package coffeeshout.minigame.ui.request;

import coffeeshout.minigame.ui.command.MiniGameCommand;
import coffeeshout.minigame.ui.request.command.SelectCardCommand;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public enum CommandType {

    // 카드 게임
    SELECT_CARD(SelectCardCommand.class),
    ;

    private final Class<?> commandRequestClass;

    CommandType(Class<?> commandRequestClass) {
        this.commandRequestClass = commandRequestClass;
    }

    public MiniGameCommand toCommandRequest(ObjectMapper objectMapper, JsonNode jsonNode){
        return switch (this){
            case SELECT_CARD -> (SelectCardCommand) objectMapper.convertValue(jsonNode, this.commandRequestClass);
            default -> throw new IllegalArgumentException("지원하지 않는 명령 타입입니다: " + this);
        };
    }
}
