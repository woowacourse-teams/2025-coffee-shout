package coffeeshout.admin.overview.ui.response;

import coffeeshout.admin.overview.domain.GamePlayStat;
import coffeeshout.minigame.domain.MiniGameType;

/**
 * @param label 화면에 그대로 찍는 한글 이름. {@link MiniGameType} 이 이미 들고 있는 값이다.
 *              <p>프론트가 enum 이름과 한글 이름의 대응표를 따로 들고 있었는데, 게임이
 *              늘어나자 빠진 항목이 영문 그대로 노출됐다(LADDER_GAME). 같은 사실을 두 곳에
 *              적어 두면 한쪽만 고치는 날이 온다. 서버가 가진 것을 그대로 내려보낸다.
 * @param plays 완료된 판 수. 시작만 하고 만 게임은 DB 에 행이 없어 셀 수 없다.
 * @param share 전체 대비 비중. 0.0 ~ 1.0
 */
public record GamePlayStatResponse(MiniGameType miniGameType, String label, long plays, double share) {

    public static GamePlayStatResponse from(GamePlayStat stat) {
        return new GamePlayStatResponse(stat.miniGameType(), stat.miniGameType().label, stat.plays(), stat.share());
    }
}
