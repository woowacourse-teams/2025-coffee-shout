package coffeeshout.ui.response;

import coffeeshout.domain.Card;
import coffeeshout.domain.CardGame;
import coffeeshout.domain.Player;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
                        findCardHolderId(cardGame, card)
                ));

        return playerSelections;
    }

    private static Long findCardHolderId(CardGame cardGame, Card card) {
        for (Entry<Player, List<Card>> playerCardsEntry : cardGame.getPlayerCards().entrySet()) {
            if (playerCardsEntry.getValue().get(cardGame.getRound().getValue()).equals(card)) {
                return playerCardsEntry.getKey().getId();
            }
        }
        return null;
    }

    private static Map<Long, Integer> generatePlayerScores(CardGame cardGame) {
        final Map<Long, Integer> scores = new HashMap<>();

        cardGame.calculateScores()
                .forEach((player, score) -> scores.put(player.getId(), score.getResult()));

        return scores;
    }
}
