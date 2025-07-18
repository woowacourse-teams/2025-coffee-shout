package coffeeshout.minigame.ui;

import coffeeshout.minigame.domain.MiniGameResult;
import java.util.List;

public record MiniGameRanksMessage(
        List<MiniGameRankMessage> ranks
) {

    public static MiniGameRanksMessage from(final MiniGameResult miniGameResult) {
        final List<MiniGameRankMessage> message = miniGameResult.getRank().entrySet()
                .stream()
                .map(entry -> new MiniGameRankMessage(entry.getValue(), entry.getKey().getName()))
                .toList();

        return new MiniGameRanksMessage(message);
    }
}
