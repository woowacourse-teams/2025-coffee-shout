package coffeeshout.room.ui.response;

import coffeeshout.room.domain.player.Player;
import coffeeshout.room.domain.roulette.Probability;
import generator.annotaions.WebSocketMessage;
import java.util.Map.Entry;

@WebSocketMessage
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
