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

/**
 * 작업함의 종류 표식.
 *
 * <p>짧게 둔다. 표의 첫 열이라 길면 내용 열을 밀어낸다. "격리 메시지"가 아니라 "격리"인
 * 것도 그래서다. 무엇이 격리됐는지는 바로 옆 칸이 말한다.
 */
const INBOX_KIND: Record<string, string> = {
  REPORT: '신고',
  NICKNAME: '닉네임',
  DEAD_LETTER: '격리',
};

/**
 * 닉네임 검열 대기의 두 상태.
 *
 * <p>세그먼트에 들어가는 짧은 말이라 "AI가" 를 떼었다. 세그먼트 옆 설명이 누가 걸렀는지를
 * 이미 말하고 있고, 두 칸에 같은 주어가 반복되면 정작 다른 부분이 눈에 안 들어온다.
 */
const NICKNAME_AUDIT_STATUS: Record<string, string> = {
  UNAUDITED: '판정 전',
  FLAGGED: '걸러냄',
  PENDING: '판단 못함',
  CLEAN: '정상',
  ALLOWED: '허용함',
  BLOCKED: '차단함',
};

/** 방이 어디까지 갔는지. 상태 배지가 쓴다. */
const ROOM_STATE: Record<string, string> = {
  READY: '시작 안 함',
  PLAYING: '게임 중',
  SCORE_BOARD: '점수판',
  ROULETTE: '룰렛',
  DONE: '완주',
};

/** 소셜 제공자. 서버는 registrationId 소문자를 준다. */
const PROVIDER: Record<string, string> = {
  google: '구글',
  kakao: '카카오',
  naver: '네이버',
};

export function reportCategoryLabel(value: string): string {
  return REPORT_CATEGORY[value] ?? value;
}

export function inboxKindLabel(value: string): string {
  return INBOX_KIND[value] ?? value;
}

export function miniGameLabel(value: string): string {
  return MINI_GAME[value] ?? value;
}

export function nicknameAuditStatusLabel(value: string): string {
  return NICKNAME_AUDIT_STATUS[value] ?? value;
}

export function roomStateLabel(value: string): string {
  return ROOM_STATE[value] ?? value;
}

export function providerLabel(value: string): string {
  return PROVIDER[value] ?? value;
}
