package coffeeshout.admin.overview.domain;

/**
 * 방 진행 퍼널. 이 서비스에서 "얼마나 잘 되고 있나"에 가장 직접적으로 답하는 지표다.
 *
 * <p>단계는 {@code room_session.roomStatus}로 판정한다. 그 값이 방이 도달한 최대 단계를
 * 그대로 들고 있기 때문이다. READY 로 생성되고, 게임이 시작되면 PLAYING, 룰렛에 들어가면
 * ROULETTE, 끝나면 DONE 으로 갱신된다.
 *
 * <p><b>SCORE_BOARD 는 세지 않는다.</b> 그 상태는 메모리 안에서만 존재하고 DB 로 내려가지
 * 않는다(Room.java 가 필드만 바꾸고 RoomStatusPort 를 부르지 않는다). 없는 단계를 표에
 * 그려 두면 언제나 0 이 찍혀 지표를 못 믿게 된다.
 *
 * <p><b>mini_game_play 로 "게임 시작"을 세지 않는다.</b> 그 행은 게임이 끝날 때
 * 결과와 함께 저장되므로(MiniGameResultSaveEventListener) 시작과 완료를 구분하지 못한다.
 *
 * @param created      방 생성 수
 * @param joined       방장 외에 한 명이라도 더 들어온 방
 *                     <p>2인이라는 숫자가 기준인 것이 아니다. 2인부터 게임이 되므로
 *                     이 단계가 재는 것은 "임계 인원을 넘었나"가 아니라
 *                     <b>"만들어 놓고 아무도 안 왔나"</b>다. 방 수만 세면 안 보인다.
 * @param gameStarted  게임을 시작한 방 (roomStatus 가 READY 를 벗어남)
 * @param rouletteReached 룰렛까지 간 방
 * @param completed    끝까지 간 방 (DONE)
 */
public record RoomFunnel(
        long created,
        long joined,
        long gameStarted,
        long rouletteReached,
        long completed
) {

    public static RoomFunnel empty() {
        return new RoomFunnel(0, 0, 0, 0, 0);
    }

    /** 방 생성 대비 완주 비율. 0.0 ~ 1.0. 생성이 없으면 0. */
    public double completionRate() {
        return created == 0 ? 0 : (double) completed / created;
    }
}
