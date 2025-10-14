import CardIcon from '@/assets/card-icon.svg';
import RacingIcon from '@/assets/racing-icon.svg';

/**
 * 전체 미니 게임 공통 타입
 */

export const MINI_GAME_NAME_MAP = {
  CARD_GAME: '카드게임',
  RACING_GAME: '레이싱게임',
  // '31_GAME': '랜덤 31222',
} as const;

export type MiniGameType = keyof typeof MINI_GAME_NAME_MAP;

export const MINI_GAME_DESCRIPTION_MAP: Record<MiniGameType, string[]> = {
  CARD_GAME: ['2라운드 동안 매번 카드 1장씩 뒤집어', '가장 높은 점수를 내보세요!'],
  RACING_GAME: ['화면을 클릭해 속도를 높여서', '가장 먼저 도착하세요!'],
};

export const MINI_GAME_ICON_MAP: Record<MiniGameType, string> = {
  CARD_GAME: CardIcon,
  RACING_GAME: RacingIcon,
};
