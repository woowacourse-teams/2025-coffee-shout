package coffeeshout.minigame.racinggame.domain.dto;

import coffeeshout.minigame.racinggame.domain.RacingGameState;
import generator.annotaions.JsonSchemaEnumType;

public record RacingGameStateResponse(
        @JsonSchemaEnumType(enumType = RacingGameState.class) String state
) {
}
