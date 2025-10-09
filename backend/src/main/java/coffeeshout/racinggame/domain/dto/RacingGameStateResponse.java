package coffeeshout.racinggame.domain.dto;

import coffeeshout.racinggame.domain.RacingGameState;
import generator.annotaions.JsonSchemaEnumType;

public record RacingGameStateResponse(
        @JsonSchemaEnumType(enumType = RacingGameState.class) String state
) {
}
