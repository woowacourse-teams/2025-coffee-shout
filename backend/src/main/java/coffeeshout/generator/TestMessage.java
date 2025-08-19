package coffeeshout.generator;

import coffeeshout.minigame.domain.task.CardGameTaskType;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDescription;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaExamples;
import javax.validation.constraints.NotNull;

@WebsocketMessage
public record TestMessage(

        @JsonSchemaEnumType(CardGameTaskType.class)
        String playerName,

        @NotNull
        @JsonSchemaDescription("메뉴 ID")
        @JsonSchemaExamples({"101", "202"})
        Long menuId
) {
}
