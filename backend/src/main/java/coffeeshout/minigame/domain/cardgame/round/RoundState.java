package coffeeshout.minigame.domain.cardgame.round;

import lombok.Getter;

/**
 * 카드게임의 현재 라운드와 단계 정보를 관리하는 클래스
 */
@Getter
public class RoundState {
    private final int roundNumber;
    private final RoundPhase phase;

    public RoundState(int roundNumber, RoundPhase phase) {
        this.roundNumber = roundNumber;
        this.phase = phase;
    }

    /**
     * 다음 단계로 전환합니다.
     *
     * @param maxRounds 최대 라운드 수
     * @return 다음 단계의 RoundState
     */
    public RoundState nextPhase(int maxRounds) {
        RoundPhase nextPhase = getNextPhase(maxRounds);
        int nextRoundNumber = getNextRoundNumber(nextPhase);

        return new RoundState(nextRoundNumber, nextPhase);
    }

    private RoundPhase getNextPhase(int maxRounds) {
        return switch (phase) {
            case READY -> RoundPhase.LOADING;
            case LOADING -> RoundPhase.PLAYING;
            case PLAYING -> RoundPhase.SCORING;
            case SCORING -> getRoundPhase(maxRounds);
            case DONE -> RoundPhase.DONE;
        };
    }

    private RoundPhase getRoundPhase(int maxRounds) {
        // 현재 라운드가 최대 라운드와 같다면 게임 종료
        if (roundNumber >= maxRounds) {
            return RoundPhase.DONE;
        }
        return RoundPhase.LOADING;
    }

    private int getNextRoundNumber(RoundPhase nextPhase) {
        // SCORING -> LOADING으로 넘어갈 때 라운드 번호 증가
        if (phase == RoundPhase.SCORING && nextPhase == RoundPhase.LOADING) {
            return roundNumber + 1;
        }
        return roundNumber;
    }

    /**
     * 게임이 완전히 종료되었는지 확인
     */
    public boolean isGameFinished() {
        return phase == RoundPhase.DONE;
    }

    /**
     * 현재 플레이 중인 단계인지 확인
     */
    public boolean isPlayingPhase() {
        return phase == RoundPhase.PLAYING;
    }

    @Override
    public String toString() {
        return String.format("Round %d - %s", roundNumber, phase);
    }
}
