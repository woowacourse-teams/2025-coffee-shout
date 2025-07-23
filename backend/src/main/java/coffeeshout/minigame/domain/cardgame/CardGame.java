package coffeeshout.minigame.domain.cardgame;

import coffeeshout.minigame.domain.MiniGameResult;
import coffeeshout.minigame.domain.MiniGameScore;
import coffeeshout.minigame.domain.cardgame.card.Card;
import coffeeshout.minigame.domain.cardgame.card.CardGameDeckGenerator;
import coffeeshout.minigame.domain.cardgame.card.Deck;
import coffeeshout.player.domain.Player;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.Getter;
import lombok.NonNull;

@Getter
public class CardGame {

    private static final int ADDITION_CARD_COUNT = 6;
    private static final int MULTIPLIER_CARD_COUNT = 3;

    private final PlayerHands playerHands;
    private final Deck deck;
    private CardGameRound round;
    private CardGameState state;

    public CardGame(
            @NonNull CardGameDeckGenerator deckGenerator,
            @NonNull List<Player> players
    ) {
        this.playerHands = new PlayerHands(players);
        this.deck = deckGenerator.generate(ADDITION_CARD_COUNT, MULTIPLIER_CARD_COUNT);
        this.round = CardGameRound.READY;
        this.state = CardGameState.READY;
    }

    public MiniGameResult getResult() {
        return MiniGameResult.from(calculateScores());
    }


    public void startRound() {
        this.round = round.next();
        deck.shuffle();
        this.state = CardGameState.PLAYING;
    }

    public void selectCard(Player player, Integer cardIndex) {

        playerHands.put(player, deck.pick(cardIndex));
    }

    public Map<Player, MiniGameScore> calculateScores() {
        return playerHands.scoreByPlayer();
    }

    public boolean isFinished(CardGameRound targetRound) {
        return round == targetRound && playerHands.isRoundFinished();
    }

    public Player findPlayerByName(String name) {
        return playerHands.findPlayerByName(name);
    }

    public boolean isSecondRound() {
        return round == CardGameRound.SECOND;
    }

    public void assignRandomCardsToUnselectedPlayers() {
        List<Player> unselectedPlayers = playerHands.getUnselectedPlayers(round);
        for (Player player : unselectedPlayers) {
            Card card = deck.pickRandom();
            playerHands.put(player, card);
        }
    }

    public Optional<Player> findCardOwnerInCurrentRound(Card card) {
        return playerHands.findCardOwner(card, round);
    }

    public void changeState(CardGameState state) {
        this.state = state;
    }
}
