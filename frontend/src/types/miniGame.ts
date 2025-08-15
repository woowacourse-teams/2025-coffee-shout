import { CardGameRound } from '@/constants/miniGame';

export const MINI_GAME_NAME_MAP = {
  CARD_GAME: '카드게임',
  // '31_GAME': '랜덤 31222',
} as const;

export type MiniGameType = keyof typeof MINI_GAME_NAME_MAP;

export const MINI_GAME_DESCRIPTION: Record<MiniGameType, string[]> = {
  CARD_GAME: ['2라운드 동안 매번 카드 1장씩 뒤집어', '가장 높은 점수를 내보세요!'],
};

export type CardGameState = 'READY' | 'LOADING' | 'PREPARE' | 'PLAYING' | 'SCORE_BOARD' | 'DONE';

export type CardType = 'ADDITION' | 'MULTIPLIER';

// TODO: 백엔드가 수정한 값대로 변경사항 반영 필요
export type AdditionValue = -40 | -30 | -20 | -10 | 0 | 10 | 20 | 30 | 40;
export type MultiplierValue = -1 | 0 | 2 | 4;

export type CardValue = AdditionValue | MultiplierValue;

export type AdditionCard = {
  type: 'ADDITION';
  value: AdditionValue;
};

export type MultiplierCard = {
  type: 'MULTIPLIER';
  value: MultiplierValue;
};

export type Card = AdditionCard | MultiplierCard;

export type CardInfo = {
  cardType: CardType;
  value: CardValue;
  selected: boolean;
  playerName: string | null;
  colorIndex: number;
};

export type SelectedCardInfo = Record<
  CardGameRound,
  {
    isSelected: boolean;
    type: CardType | null;
    value: CardValue | null;
  }
>;

export type PlayerScore = {
  playerName: string;
  score: number;
};

export type PlayerRank = {
  playerName: string;
  rank: number;
};
