package coffeeshout.ui.response;

import coffeeshout.domain.CardGame;
import java.util.HashMap;
import java.util.Map;

public record MiniGameStateMessage(
        Long roomId,
        int currentRound,
        Map<CardDto, Long> playerSelections,
        Map<Long, Integer> scores,
        Boolean allSelected
) {

    public static Object of(final CardGame cardGame, final Long roomId) {

        final Map<CardDto, Long> playerSelections = generatePlayerSelections(cardGame);
        final Map<Long, Integer> scores = generatePlayerScores(cardGame);

        return new MiniGameStateMessage(
                roomId,
                cardGame.getRound().getValue(),
                playerSelections,
                scores,
                cardGame.isFirstRoundFinished()
        );
    }

    private static Map<CardDto, Long> generatePlayerSelections(CardGame cardGame) {
        final Map<CardDto, Long> playerSelections = new HashMap<>();

        cardGame.getCards()
                .forEach(card -> playerSelections.put(
                        CardDto.from(card),
                        cardGame.findCardHolder(card, cardGame.getRound().getValue()).getId()
                ));

        return playerSelections;
    }

    private static Map<Long, Integer> generatePlayerScores(CardGame cardGame) {
        final Map<Long, Integer> scores = new HashMap<>();

        cardGame.calculateScores()
                .forEach((player, score) -> scores.put(player.getId(), score.getResult()));

        return scores;
    }
}
