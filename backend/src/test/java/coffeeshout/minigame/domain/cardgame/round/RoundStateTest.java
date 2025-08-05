package coffeeshout.minigame.domain.cardgame.round;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class RoundStateTest {

    @Test
    void 라운드_1에서_READY_다음_단계는_LOADING이다() {
        // given
        RoundState state = new RoundState(1, RoundPhase.READY);
        
        // when
        RoundState nextState = state.nextPhase(2);
        
        // then
        assertThat(nextState.getRoundNumber()).isEqualTo(1);
        assertThat(nextState.getPhase()).isEqualTo(RoundPhase.LOADING);
    }
    
    @Test
    void LOADING_다음_단계는_PLAYING이다() {
        // given
        RoundState state = new RoundState(1, RoundPhase.LOADING);
        
        // when
        RoundState nextState = state.nextPhase(2);
        
        // then
        assertThat(nextState.getRoundNumber()).isEqualTo(1);
        assertThat(nextState.getPhase()).isEqualTo(RoundPhase.PLAYING);
    }
    
    @Test
    void PLAYING_다음_단계는_SCORING이다() {
        // given
        RoundState state = new RoundState(1, RoundPhase.PLAYING);
        
        // when
        RoundState nextState = state.nextPhase(2);
        
        // then
        assertThat(nextState.getRoundNumber()).isEqualTo(1);
        assertThat(nextState.getPhase()).isEqualTo(RoundPhase.SCORING);
    }
    
    @Test
    void 라운드_1_SCORING_다음_단계는_라운드_2_LOADING이다() {
        // given
        RoundState state = new RoundState(1, RoundPhase.SCORING);
        
        // when
        RoundState nextState = state.nextPhase(2);
        
        // then
        assertThat(nextState.getRoundNumber()).isEqualTo(2);
        assertThat(nextState.getPhase()).isEqualTo(RoundPhase.LOADING);
    }
    
    @Test
    void 최대_라운드에서_SCORING_다음_단계는_DONE이다() {
        // given
        RoundState state = new RoundState(2, RoundPhase.SCORING);
        
        // when
        RoundState nextState = state.nextPhase(2);
        
        // then
        assertThat(nextState.getRoundNumber()).isEqualTo(2);
        assertThat(nextState.getPhase()).isEqualTo(RoundPhase.DONE);
    }
    
    @Test
    void DONE_다음_단계는_계속_DONE이다() {
        // given
        RoundState state = new RoundState(2, RoundPhase.DONE);
        
        // when
        RoundState nextState = state.nextPhase(2);
        
        // then
        assertThat(nextState.getRoundNumber()).isEqualTo(2);
        assertThat(nextState.getPhase()).isEqualTo(RoundPhase.DONE);
    }
    
    @Test
    void _3라운드_게임에서_라운드_3_SCORING_다음_단계는_DONE이다() {
        // given
        RoundState state = new RoundState(3, RoundPhase.SCORING);
        
        // when
        RoundState nextState = state.nextPhase(3);
        
        // then
        assertThat(nextState.getRoundNumber()).isEqualTo(3);
        assertThat(nextState.getPhase()).isEqualTo(RoundPhase.DONE);
    }
    
    @Test
    void PLAYING_단계에서_isPlayingPhase는_true를_반환한다() {
        // given
        RoundState state = new RoundState(1, RoundPhase.PLAYING);
        
        // when & then
        assertThat(state.isPlayingPhase()).isTrue();
    }
    
    @Test
    void LOADING_단계에서_isPlayingPhase는_false를_반환한다() {
        // given
        RoundState state = new RoundState(1, RoundPhase.LOADING);
        
        // when & then
        assertThat(state.isPlayingPhase()).isFalse();
    }
    
    @Test
    void DONE_단계에서_isGameFinished는_true를_반환한다() {
        // given
        RoundState state = new RoundState(2, RoundPhase.DONE);
        
        // when & then
        assertThat(state.isGameFinished()).isTrue();
    }
    
    @Test
    void PLAYING_단계에서_isGameFinished는_false를_반환한다() {
        // given
        RoundState state = new RoundState(1, RoundPhase.PLAYING);
        
        // when & then
        assertThat(state.isGameFinished()).isFalse();
    }
}
