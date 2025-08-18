/**
 * 미니 게임 라운드 타입
 */

export const ROUND_MAP = {
  FIRST: 1,
  SECOND: 2,
} as const;

export type RoundType = keyof typeof ROUND_MAP;
