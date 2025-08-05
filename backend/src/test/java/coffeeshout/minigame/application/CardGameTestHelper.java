package coffeeshout.minigame.application;

import coffeeshout.minigame.domain.cardgame.CardGame;
import coffeeshout.minigame.domain.cardgame.round.RoundPhase;

/**
 * 카드게임 테스트를 위한 유틸리티 클래스
 */
public class CardGameTestHelper {
    
    /**
     * 카드게임을 PLAYING 상태로 변경합니다.
     * 테스트에서 상태 변경이 필요할 때 사용합니다.
     */
    public static void setPlayingState(CardGame cardGame) {
        // READY → LOADING → PLAYING 순서로 변경
        if (cardGame.getCurrentPhase() == RoundPhase.READY) {
            cardGame.setRoundState(cardGame.getRoundState().nextPhase(cardGame.getMaxRounds())); // LOADING
        }
        if (cardGame.getCurrentPhase() == RoundPhase.LOADING) {
            cardGame.setRoundState(cardGame.getRoundState().nextPhase(cardGame.getMaxRounds())); // PLAYING
        }
    }
    
    /**
     * 카드게임을 특정 상태로 변경합니다.
     */
    public static void setPhase(CardGame cardGame, RoundPhase targetPhase) {
        while (cardGame.getCurrentPhase() != targetPhase && cardGame.getCurrentPhase() != RoundPhase.DONE) {
            cardGame.setRoundState(cardGame.getRoundState().nextPhase(cardGame.getMaxRounds()));
        }
    }
    
    /**
     * 현재 라운드 상태를 문자열로 반환합니다. (디버깅용)
     */
    public static String getRoundStateInfo(CardGame cardGame) {
        return String.format("Round %d - %s", 
            cardGame.getCurrentRoundNumber(), 
            cardGame.getCurrentPhase());
    }
}
