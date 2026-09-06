package coffeeshout.admin.overview.ui.response;

import coffeeshout.admin.overview.domain.GamePlayStat;
import coffeeshout.minigame.domain.MiniGameType;

/**
 * @param plays 완료된 판 수. 시작만 하고 만 게임은 DB 에 행이 없어 셀 수 없다.
 * @param share 전체 대비 비중. 0.0 ~ 1.0
 */
public record GamePlayStatResponse(MiniGameType miniGameType, long plays, double share) {

    public static GamePlayStatResponse from(GamePlayStat stat) {
        return new GamePlayStatResponse(stat.miniGameType(), stat.plays(), stat.share());
    }
}
