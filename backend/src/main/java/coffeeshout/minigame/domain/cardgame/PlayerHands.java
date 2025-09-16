package coffeeshout.minigame.domain.cardgame;

import coffeeshout.minigame.domain.MiniGameScore;
import coffeeshout.minigame.domain.cardgame.card.Card;
import coffeeshout.room.domain.player.Player;
import coffeeshout.room.domain.player.PlayerName;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class PlayerHands {

    private List<PlayerHand> playerHands;

    public PlayerHands(List<Player> players) {
        this.playerHands = players.stream()
                .map(PlayerHand::new)
                .collect(Collectors.toList());
    }

    public void put(PlayerName playerName, Card card) {
        findPlayerHandByName(playerName).putCard(card);
    }

    public int totalHandSize() {
        return playerHands.stream()
                .map(PlayerHand::handSize)
                .reduce(0, Integer::sum);

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

    public Player findPlayerByName(PlayerName name) {
        return findPlayerHandByName(name).getPlayer();
    }

    public Map<Player, MiniGameScore> scoreByPlayer() {
        return playerHands.stream().collect(Collectors.toMap(
                PlayerHand::getPlayer,
                PlayerHand::calculateScore
        ));
    }

    public List<Player> getUnselectedPlayers(CardGameRound round) {
        final List<Player> players = new ArrayList<>();
        playerHands.forEach(playerHand -> {
            if (!playerHand.isSelected(round)) {
                players.add(playerHand.getPlayer());
            }
        });
        return players;
    }

    public Optional<Player> findCardOwner(Card card, CardGameRound round) {
        return playerHands.stream()
                .filter(playerHand -> playerHand.isAssign(card, round))
                .findFirst()
                .map(PlayerHand::getPlayer);
    }

    private PlayerHand findPlayerHandByName(PlayerName playerName) {
        return playerHands.stream()
                .filter(playerHand -> playerHand.isSameName(playerName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }
}
