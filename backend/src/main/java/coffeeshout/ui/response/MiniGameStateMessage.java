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
        Map<Card, String> playerSelections,
        Map<String, Integer> scores,
        Boolean allSelected
) {

    public static Object of(final CardGame cardGame, final Long roomId) {

        final Map<Card, String> playerSelections = generatePlayerSelections(cardGame);
        final Map<String, Integer> scores = generatePlayerScores(cardGame);

        return new MiniGameStateMessage(
                roomId,
                cardGame.getRound().getValue(),
                playerSelections,
                scores,
                cardGame.isFirstRoundFinished()
        );
    }

    private static Map<Card, String> generatePlayerSelections(CardGame cardGame) {
        final Map<Card, String> playerSelections = new HashMap<>();

        cardGame.getCards()
                .forEach(card -> playerSelections.put(
                        card,
                        findCardHolderName(cardGame, card)
                ));

        return playerSelections;
    }

    private static String findCardHolderName(CardGame cardGame, Card card) {
        for (Entry<Player, List<Card>> playerCardsEntry : cardGame.getPlayerCards().entrySet()) {
            if (hasSameCardInCurrentRound(cardGame, card, playerCardsEntry)) {
                return playerCardsEntry.getKey().getName();
            }
        }
        return null;
    }

    private static boolean hasSameCardInCurrentRound(CardGame cardGame, Card card,
                                                     Entry<Player, List<Card>> playerCardsEntry) {
        return playerCardsEntry.getValue().get(cardGame.getRound().getValue()).equals(card);
    }

    private static Map<String, Integer> generatePlayerScores(CardGame cardGame) {
        final Map<String, Integer> scores = new HashMap<>();

        cardGame.calculateScores()
                .forEach((player, score) -> scores.put(player.getName(), score.getResult()));

        return scores;
    }
}
