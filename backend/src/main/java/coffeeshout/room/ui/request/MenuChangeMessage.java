package coffeeshout.room.ui.request;

import coffeeshout.generator.JsonSchemaEnumType;
import coffeeshout.generator.WebsocketMessage;
import coffeeshout.minigame.domain.cardgame.CardGameTaskType;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDescription;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaExamples;
import javax.validation.constraints.NotNull;

@WebsocketMessage
public record MenuChangeMessage(

        @JsonSchemaEnumType(enumType = CardGameTaskType.class)
        String playerName,

        @NotNull
        @JsonSchemaDescription("메뉴 ID")
        @JsonSchemaExamples({"101", "202"})
        Long menuId
) {
}
