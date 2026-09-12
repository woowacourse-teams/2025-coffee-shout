package coffeeshout.admin.overview.domain;

import coffeeshout.minigame.domain.MiniGameType;

/**
 * 게임별 플레이 집계.
 *
 * <p><b>완료된 게임만 센다.</b> mini_game_play 행이 게임 종료 시점에 결과와 함께
 * 저장되기 때문이다(MiniGameResultSaveEventListener). 시작만 하고 만 게임은 애초에
 * 행이 없어서 셀 수 없다.
 *
 * <p>그래도 값이 있다. "블록쌓기를 아무도 안 고른다" 같은 사실은 이 숫자로만 보인다.
 * Grafana 는 게임 타입을 모른다.
 *
 * @param share 전체 대비 비중. 0.0 ~ 1.0
 */
public record GamePlayStat(MiniGameType miniGameType, long plays, double share) {}
