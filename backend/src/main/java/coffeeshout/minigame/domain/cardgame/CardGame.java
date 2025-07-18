package coffeeshout.minigame.domain.cardgame;

import coffeeshout.minigame.domain.MiniGameResult;
import coffeeshout.minigame.domain.cardgame.card.Deck;
import coffeeshout.player.domain.Player;
import coffeeshout.room.domain.Playable;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.NonNull;

@Getter
public class CardGame implements Playable {

    private final PlayerHands playerHands;
    private final Deck deck;
    private CardGameRound round;

    public CardGame(
            @NonNull Deck deck,
            @NonNull List<Player> players
    ) {
        this.playerHands = new PlayerHands(players);
        this.deck = deck;
        this.round = CardGameRound.FIRST;
    }

    @Override
    public void start() {
        initGame();
    }

    @Override
    public MiniGameResult getResult() {
        return MiniGameResult.from(calculateScores());
    }

    public void nextRound() {
        this.round = round.next();
    }

    public void initGame() {
        deck.shuffle();
    }

    public void selectCard(Player player, Integer cardIndex) {
        playerHands.put(player, deck.pick(cardIndex));
    }

    public Map<Player, CardGameScore> calculateScores() {
        return playerHands.scoreByPlayer();
    }

    public boolean isFinished(CardGameRound targetRound) {
        return round == targetRound && playerHands.isRoundFinished();
    }

    public Player findPlayerByName(String name) {
        return playerHands.findPlayerByName(name);
    }

    public boolean isFirstRound() {
        return round == CardGameRound.FIRST;
    }

    public boolean isSecondRound() {
        return round == CardGameRound.SECOND;
    }
}
