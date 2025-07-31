package coffeeshout.minigame.domain.temp;

import coffeeshout.minigame.domain.cardgame.CardGameRound;
import coffeeshout.minigame.domain.cardgame.CardGameState;
import java.util.Arrays;

public enum CardGameTaskInfo {

    FIRST_ROUND_LOADING(CardGameState.LOADING, CardGameRound.FIRST),
    FIRST_ROUND_PLAYING(CardGameState.PLAYING, CardGameRound.FIRST),
    FIRST_ROUND_SCORE_BOARD(CardGameState.SCORE_BOARD, CardGameRound.FIRST),
    SECOND_ROUND_LOADING(CardGameState.LOADING, CardGameRound.SECOND),
    SECOND_ROUND_PLAYING(CardGameState.PLAYING, CardGameRound.SECOND),
    SECOND_ROUND_SCORE_BOARD(CardGameState.SCORE_BOARD, CardGameRound.SECOND),
    GAME_FINISH(CardGameState.DONE, CardGameRound.END),
    ;

    private final CardGameState state;
    private final CardGameRound round;

    CardGameTaskInfo(CardGameState state, CardGameRound round) {
        this.state = state;
        this.round = round;
    }

    public static CardGameTaskInfo getPlayingState(CardGameRound round) {
        return Arrays.stream(values())
                .filter(taskInfo -> taskInfo.state == CardGameState.PLAYING && taskInfo.round == round)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("해당 라운드는 PLAYING이 존재하지 않습니다." + round));
    }
}
