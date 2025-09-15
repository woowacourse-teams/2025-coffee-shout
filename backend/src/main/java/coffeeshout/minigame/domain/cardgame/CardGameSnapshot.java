package coffeeshout.minigame.domain.cardgame;

import coffeeshout.minigame.domain.cardgame.card.Card;
import java.util.List;
import java.util.Map;

/**
 * 카드게임의 현재 상태를 담는 스냅샷 클래스
 * Redis 동기화용으로 사용됨
 */
public record CardGameSnapshot(
    CardGameState state,
    CardGameRound round,
    Map<String, Integer> playerCardSelections, // 플레이어별 선택한 카드 인덱스
    List<Card> availableCards // 현재 사용 가능한 카드들
) {}
