package coffeeshout.minigame.ui;

import coffeeshout.minigame.domain.cardgame.CardGame;
import coffeeshout.minigame.domain.cardgame.card.Card;
import coffeeshout.player.domain.Player;
import java.util.List;
import java.util.Optional;
import lombok.NonNull;

public record MiniGameStateMessage(
        int currentRound,
        List<CardInfoMessage> cardInfoMessages,
        Boolean allSelected
) {
    public record CardInfoMessage(
            String cardType,
            int value,
            boolean selected,
            String playerName
    ) {
        public static List<CardInfoMessage> from(@NonNull CardGame cardGame) {
            return cardGame.getDeck().getCards().stream()
                    .map(card -> {
                        Optional<Player> player = cardGame.findCardOwnerInCurrentRound(card);
                        String name = player.map(Player::getName).orElse(null);
                        return CardInfoMessage.of(card, player.isPresent(), name);
                    }).toList();
        }

        public static CardInfoMessage of(@NonNull Card card, boolean isSelected, String name) {
            return new CardInfoMessage(
                    card.getType().name(),
                    card.getValue(),
                    isSelected,
                    name
            );
        }
    }

    public static MiniGameStateMessage from(@NonNull CardGame cardGame) {
        return new MiniGameStateMessage(
                cardGame.getRound().toInteger(),
                CardInfoMessage.from(cardGame),
                cardGame.getPlayerHands().isRoundFinished()
        );
    }
}
