/**
 * 서버 enum 을 화면에 쓸 한국어로 바꾼다.
 *
 * <p>지금까지는 `BUG`, `SUGGESTION` 이 표에 그대로 찍혔다. 한글 화면 한가운데 대문자
 * 영문이 서 있으면 그 열만 다른 시스템에서 온 것처럼 보이고, 운영자가 아닌 사람에게
 * 화면을 보여줄 때마다 무슨 뜻인지 설명해야 한다.
 *
 * <p>모르는 값은 <b>원문 그대로 내보낸다.</b> 빈 칸이나 "기타" 로 바꾸면 서버에 새 값이
 * 생긴 것을 화면에서 알아챌 방법이 사라진다. 낯선 영문이 보이는 편이 낫다.
 */

const REPORT_CATEGORY: Record<string, string> = {
  BUG: '버그',
  SUGGESTION: '건의',
  GAME_REQUEST: '게임 요청',
  OTHER: '기타',
};

const MINI_GAME: Record<string, string> = {
  CARD_GAME: '카드게임',
  RACING_GAME: '레이싱',
  SPEED_TOUCH: '스피드터치',
  BLIND_TIMER: '블라인드타이머',
  BLOCK_STACKING: '블록쌓기',
  LADDER_GAME: '사다리타기',
  NUNCHI_GAME: '눈치게임',
  WORM_GAME: '지렁이 게임',
};

export function reportCategoryLabel(value: string): string {
  return REPORT_CATEGORY[value] ?? value;
}

export function miniGameLabel(value: string): string {
  return MINI_GAME[value] ?? value;
}
