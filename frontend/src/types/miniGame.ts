export const MINI_GAME_NAME_MAP = {
  CARD_GAME: '카드게임',
  // '31_GAME': '랜덤 31',
} as const;

export type MiniGameType = keyof typeof MINI_GAME_NAME_MAP;

export type CardGameState = 'READY' | 'LOADING' | 'PLAYING' | 'SCORE_BOARD' | 'DONE';

export type CardGameRound = 'FIRST' | 'SECOND';

export type CardType = 'ADDITION' | 'MULTIPLIER';

export type CardInfo = {
  cardType: CardType;
  value: number;
  selected: boolean;
  playerName: string | null;
};

export type CardGameStateData = {
  cardGameState: CardGameState;
  currentRound: CardGameRound;
  cardInfoMessages: CardInfo[];
  allSelected: boolean;
};

export type PlayerScore = {
  playerName: string;
  score: number;
};

export type PlayerRank = {
  playerName: string;
  rank: number;
};
