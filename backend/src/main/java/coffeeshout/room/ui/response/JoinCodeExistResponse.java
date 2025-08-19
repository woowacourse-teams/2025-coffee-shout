package coffeeshout.room.ui.response;

import coffeeshout.generator.WebsocketMessage;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDescription;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaExamples;

@WebsocketMessage
public record JoinCodeExistResponse(
        @JsonSchemaDescription(value = "여기 9개까지 들어갈수있음")
        @JsonSchemaExamples(value = "false")
        boolean exist
) {

    public static JoinCodeExistResponse from(boolean existence) {
        return new JoinCodeExistResponse(existence);
    }
}
