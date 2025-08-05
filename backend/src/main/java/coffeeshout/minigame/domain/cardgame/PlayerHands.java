package coffeeshout.minigame.domain.cardgame;

import coffeeshout.minigame.domain.MiniGameScore;
import coffeeshout.minigame.domain.cardgame.card.Card;
import coffeeshout.room.domain.player.Player;
import coffeeshout.room.domain.player.PlayerName;
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

    public Player findPlayerByName(PlayerName name) {
        return playerHands.keySet().stream()
                .filter(player -> player.sameName(name))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }

    public Map<Player, MiniGameScore> scoreByPlayer() {
        return playerHands.entrySet().stream().collect(Collectors.toMap(
                Entry::getKey,
                entry -> entry.getValue().calculateCardGameScore()
        ));
    }
    
    // === 새로운 라운드 관리를 위한 메서드들 ===
    
    /**
     * 특정 라운드에서 모든 플레이어가 카드를 선택했는지 확인
     */
    public boolean allSelectedInCurrentRound(int roundNumber) {
        return playerHands.values().stream()
                .allMatch(hand -> hand.hasCardForRound(roundNumber));
    }
    
    /**
     * 특정 라운드에서 선택하지 않은 플레이어들 반환
     */
    public List<Player> getUnselectedPlayers(int roundNumber) {
        return playerHands.entrySet().stream()
                .filter(entry -> !entry.getValue().hasCardForRound(roundNumber))
                .map(Entry::getKey)
                .collect(Collectors.toList());
    }
    
    /**
     * 특정 라운드에서 카드 소유자 찾기
     */
    public Optional<Player> findCardOwner(Card card, int roundNumber) {
        return playerHands.entrySet().stream()
                .filter(entry -> {
                    CardHand hand = entry.getValue();
                    return hand.hasCardForRound(roundNumber) && 
                           card.equals(hand.getCardForRound(roundNumber));
                })
                .map(Entry::getKey)
                .findFirst();
    }
}
