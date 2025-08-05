package coffeeshout.minigame.domain.cardgame.round;

/**
 * 카드게임의 라운드 단계를 나타내는 열거형
 * 각 라운드는 LOADING -> PLAYING -> SCORING 순서로 진행됩니다.
 */
public enum RoundPhase {
    READY,      // 게임 시작 전 준비 상태
    LOADING,    // 라운드 로딩 중
    PLAYING,    // 카드 선택 중
    SCORING,    // 점수 계산 및 표시
    DONE        // 게임 완료
}
