package coffeeshout.minigame.domain.cardgame;

import static org.springframework.util.Assert.state;

import coffeeshout.minigame.domain.MiniGameResult;
import coffeeshout.minigame.domain.MiniGameScore;
import coffeeshout.minigame.domain.MiniGameType;
import coffeeshout.minigame.domain.cardgame.card.Card;
import coffeeshout.minigame.domain.cardgame.card.CardGameDeckGenerator;
import coffeeshout.minigame.domain.cardgame.card.Deck;
import coffeeshout.room.domain.Playable;
import coffeeshout.room.domain.player.Player;
import coffeeshout.room.domain.player.PlayerName;
import coffeeshout.room.domain.player.Players;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.Getter;
import lombok.NonNull;

@Getter
public class CardGame implements Playable {

    private static final int ADDITION_CARD_COUNT = 6;
    private static final int MULTIPLIER_CARD_COUNT = 3;

    private final Deck deck;
    private PlayerHands playerHands;
    private CardGameRound round;
    private CardGameState state;

    public CardGame(@NonNull CardGameDeckGenerator deckGenerator) {
        this.round = CardGameRound.READY;
        this.state = CardGameState.READY;
        this.deck = deckGenerator.generate(ADDITION_CARD_COUNT, MULTIPLIER_CARD_COUNT);
    }

    @Override
    public MiniGameResult getResult() {
        return MiniGameResult.from(calculateScores());
    }

    @Override
    public MiniGameType getMiniGameType() {
        return MiniGameType.CARD_GAME;
    }

    @Override
    public void startGame(Players players) {
        playerHands = new PlayerHands(players);
    }

    public void startRound() {
        deck.shuffle();
        this.round = round.next();
        this.state = CardGameState.PLAYING;
    }

    public void selectCard(Player player, Integer cardIndex) {
        state(state == CardGameState.PLAYING, "현재 게임이 진행중인 상태가 아닙니다.");
        playerHands.put(player, deck.pick(cardIndex));
    }

    public Map<Player, MiniGameScore> calculateScores() {
        return playerHands.scoreByPlayer();
    }

    public boolean isFinishedThisRound() {
        return isFinished(round);
    }

    public boolean isFinished(CardGameRound targetRound) {
        return round == targetRound && playerHands.isRoundFinished();
    }

    public Player findPlayerByName(PlayerName name) {
        return playerHands.findPlayerByName(name);
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

    public void changeScoreBoardState() {
        this.state = CardGameState.SCORE_BOARD;
    }

    public void changeLoadingState() {
        this.state = CardGameState.LOADING;
    }

    public void changeDoneState() {
        this.state = CardGameState.DONE;
    }
}
