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
        Map<Card, Long> playerSelections,
        Map<Long, Integer> scores,
        Boolean allSelected
) {

    public static Object of(final CardGame cardGame, final Long roomId) {

        final Map<Card, Long> playerSelections = generatePlayerSelections(cardGame);
        final Map<Long, Integer> scores = generatePlayerScores(cardGame);

        return new MiniGameStateMessage(
                roomId,
                cardGame.getRound().getValue(),
                playerSelections,
                scores,
                cardGame.isFirstRoundFinished()
        );
    }

    private static Map<Card, Long> generatePlayerSelections(CardGame cardGame) {
        final Map<Card, Long> playerSelections = new HashMap<>();

        cardGame.getCards()
                .forEach(card -> playerSelections.put(
                        card,
                        findCardHolderId(cardGame, card)
                ));

        return playerSelections;
    }

    private static Long findCardHolderId(CardGame cardGame, Card card) {
        for (Entry<Player, List<Card>> playerCardsEntry : cardGame.getPlayerCards().entrySet()) {
            if (hasSameCardInCurrentRound(cardGame, card, playerCardsEntry)) {
                return playerCardsEntry.getKey().getId();
            }
        }
        return null;
    }

    private static boolean hasSameCardInCurrentRound(CardGame cardGame, Card card,
                                                     Entry<Player, List<Card>> playerCardsEntry) {
        return playerCardsEntry.getValue().get(cardGame.getRound().getValue()).equals(card);
    }

    private static Map<Long, Integer> generatePlayerScores(CardGame cardGame) {
        final Map<Long, Integer> scores = new HashMap<>();

        cardGame.calculateScores()
                .forEach((player, score) -> scores.put(player.getId(), score.getResult()));

        return scores;
    }
}
