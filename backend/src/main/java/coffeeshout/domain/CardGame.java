package coffeeshout.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import lombok.Getter;

@Getter
public class CardGame implements Playable {

    private final Map<Player, List<Card>> playerCards;

    private List<Card> cards;

    private CardGameRound round;

    public CardGame(List<Player> players) {
        this.playerCards = initPlayerCards(players);
        this.round = CardGameRound.ONE;
        this.cards = CardGameDeck.spreadCards();
    }

    @Override
    public void play() {

    }

    @Override
    public MiniGameResult getResult() {
        final MiniGameResult miniGameResult = new MiniGameResult();

        final Map<Player, CardGameScore> scores = calculateScores();

        // 점수순으로 플레이어들을 정렬 (높은 점수부터)
        final List<Map.Entry<Player, CardGameScore>> sortedEntries = scores.entrySet()
                .stream()
                .sorted((entry1, entry2) -> entry2.getValue().compareTo(entry1.getValue()))
                .toList();

        // 동점자를 고려한 순위 매기기
        int currentRank = 1;
        CardGameScore previousScore = null;
        int playersWithSameRank = 0;

        for (Map.Entry<Player, CardGameScore> entry : sortedEntries) {
            final CardGameScore currentScore = entry.getValue();

            // 이전 점수와 다르면 순위 업데이트
            if (previousScore != null && currentScore.compareTo(previousScore) != 0) {
                currentRank += playersWithSameRank;
                playersWithSameRank = 0;
            }

            miniGameResult.setRank(currentRank, entry.getKey());
            playersWithSameRank++;
            previousScore = currentScore;
        }

        return miniGameResult;
    }

    public void nextRound() {
        this.round = round.next();
    }

    public void shuffle() {
        this.cards = CardGameDeck.spreadCards();
    }

    public void selectCard(Player player, Integer cardPosition) {
        playerCards.get(player).add(cards.get(cardPosition));
    }

    public Map<Player, CardGameScore> calculateScores() {
        final Map<Player, CardGameScore> scores = new HashMap<>();

        for (Entry<Player, List<Card>> playerCardEntry : playerCards.entrySet()) {
            scores.put(playerCardEntry.getKey(), sumCards(playerCardEntry.getValue()));
        }

        return scores;
    }


    public Boolean isFirstRoundFinished() {
        final Boolean allSelected = true;

        for (List<Card> cards : playerCards.values()) {
            if (cards.size() != 1) {
                return false;
            }
        }

        return allSelected;
    }

    public Boolean isSecondRoundFinished() {
        final Boolean allSelected = true;

        for (List<Card> cards : playerCards.values()) {
            if (cards.size() != 2) {
                return false;
            }
        }

        return allSelected;
    }

    private Map<Player, List<Card>> initPlayerCards(List<Player> players) {
        final Map<Player, List<Card>> playerCards = new HashMap<>();

        for (Player player : players) {
            playerCards.put(player, new ArrayList<>());
        }

        return playerCards;
    }

    private CardGameScore sumCards(List<Card> cards) {
        final CardGameScore cardGameScore = new CardGameScore();

        for (Card card : cards) {
            cardGameScore.addCard(card);
        }

        return cardGameScore;
    }
}
