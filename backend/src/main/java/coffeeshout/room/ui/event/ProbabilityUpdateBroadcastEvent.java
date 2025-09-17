package coffeeshout.room.ui.event;

import coffeeshout.room.ui.response.ProbabilityResponse;
import java.util.List;

public record ProbabilityUpdateBroadcastEvent(
        String joinCode,
        List<ProbabilityResponse> probabilities
) implements BroadcastEvent {

    public static ProbabilityUpdateBroadcastEvent create(final String joinCode, final List<ProbabilityResponse> probabilities) {
        return new ProbabilityUpdateBroadcastEvent(joinCode, probabilities);
    }

    @Override
    public BroadcastEventType getBroadcastEventType() {
        return BroadcastEventType.PROBABILITY_UPDATE;
    }
}
