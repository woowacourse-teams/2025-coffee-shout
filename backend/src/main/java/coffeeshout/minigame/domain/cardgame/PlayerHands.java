package coffeeshout.minigame.domain.cardgame;

import coffeeshout.minigame.domain.MiniGameScore;
import coffeeshout.minigame.domain.cardgame.card.Card;
import coffeeshout.player.domain.Player;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

public class PlayerHands {

    private final Map<Player, CardHand> playerHands;

    public PlayerHands(List<Player> players) {
        this.playerHands = players.stream().collect(Collectors.toMap(
                player -> player,
                player -> new CardHand()
        ));
    }

    public void put(Player player, Card card) {
        playerHands.get(player).put(card);
    }

    public int totalHandSize() {
        return playerHands.values().stream()
                .mapToInt(CardHand::size)
                .sum();
    }

    public int playerCount() {
        return playerHands.size();
    }

    public boolean isRoundFinished() {
        if (totalHandSize() == 0) {
            return false;
        }

        return totalHandSize() % playerCount() == 0;
    }

    public Player findPlayerByName(String name) {
        return playerHands.keySet().stream()
                .filter(player -> player.isSameName(name))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }

    public Map<Player, MiniGameScore> scoreByPlayer() {
        return playerHands.entrySet().stream().collect(Collectors.toMap(
                Entry::getKey,
                entry -> entry.getValue().calculateCardGameScore()
        ));
    }

    public List<Player> getUnselectedPlayers(CardGameRound round) {
        List<Player> players = new ArrayList<>();
        playerHands.forEach((player, hand) -> {
            if (!hand.isSelected(round)) {
                players.add(player);
            }
        });
        return players;
    }

    public Optional<Player> findCardOwner(Card card, CardGameRound round) {
        return playerHands.entrySet().stream()
                .filter(entry -> entry.getValue().isAssign(card, round))
                .findFirst()
                .map(Entry::getKey);
    }
}
