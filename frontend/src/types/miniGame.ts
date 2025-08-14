import { CardGameRound } from '@/constants/miniGame';

export const MINI_GAME_NAME_MAP = {
  CARD_GAME: '카드게임',
  // '31_GAME': '랜덤 31222',
} as const;

export const MINI_GAME_DESCRIPTION: Record<MiniGameType, string[]> = {
  CARD_GAME: ['2라운드 동안 매번 카드 1장씩 뒤집어', '가장 높은 점수를 내보세요!'],
};

export type MiniGameType = keyof typeof MINI_GAME_NAME_MAP;

// @todo: prepare로 수정
export type CardGameState =
  | 'READY'
  | 'LOADING'
  | 'DESCRIPTION'
  | 'PLAYING'
  | 'SCORE_BOARD'
  | 'DONE';

export type CardType = 'ADDITION' | 'MULTIPLIER';

export type CardInfo = {
  cardType: CardType;
  value: number;
  selected: boolean;
  playerName: string | null;
  colorIndex: number;
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

export type SelectedCardInfo = Record<
  CardGameRound,
  {
    isSelected: boolean;
    type: string | null;
    value: number | null;
  }
>;
