export const ROUND_NUMBER_MAP = {
  FIRST: 1,
  SECOND: 2,
} as const;

export type CardGameRound = keyof typeof ROUND_NUMBER_MAP;
export type RoundNumber = (typeof ROUND_NUMBER_MAP)[CardGameRound];
