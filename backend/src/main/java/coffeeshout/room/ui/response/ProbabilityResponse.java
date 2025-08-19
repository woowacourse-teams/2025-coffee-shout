package coffeeshout.room.ui.response;

import coffeeshout.generator.WebsocketMessage;
import coffeeshout.room.domain.player.Player;
import coffeeshout.room.domain.roulette.Probability;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaExamples;
import java.util.Map.Entry;

@WebsocketMessage
public record ProbabilityResponse(
        PlayerResponse playerResponse,
        Double probability
) {

    public static ProbabilityResponse from(Entry<Player, Probability> entry) {
        return new ProbabilityResponse(PlayerResponse.from(entry.getKey()), parseProbability(entry.getValue()));
    }

    private static Double parseProbability(Probability probability) {
        return probability.value() / 100.0;
    }
}
