export const MINI_GAME_NAME_MAP = {
  CARD_GAME: '카드게임',
} as const;

export type MiniGameType = keyof typeof MINI_GAME_NAME_MAP;
