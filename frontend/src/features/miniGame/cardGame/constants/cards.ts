export type AdditionValue = -40 | -30 | -20 | -10 | 0 | 10 | 20 | 30 | 40;
export type MultiplierValue = -1 | 0 | 2 | 4;

export type CardValue = AdditionValue | MultiplierValue;

export type CardType = 'ADDITION' | 'MULTIPLIER';

export type AdditionCard = {
  type: 'ADDITION';
  value: AdditionValue;
};

export type MultiplierCard = {
  type: 'MULTIPLIER';
  value: MultiplierValue;
};

export type Card = AdditionCard | MultiplierCard;

export const GENERAL_CARDS: AdditionCard[] = [
  { type: 'ADDITION', value: -40 },
  { type: 'ADDITION', value: -30 },
  { type: 'ADDITION', value: -20 },
  { type: 'ADDITION', value: -10 },
  { type: 'ADDITION', value: 0 },
  { type: 'ADDITION', value: 10 },
  { type: 'ADDITION', value: 20 },
  { type: 'ADDITION', value: 30 },
  { type: 'ADDITION', value: 40 },
];

export const SPECIAL_CARDS: MultiplierCard[] = [
  { type: 'MULTIPLIER', value: 4 },
  { type: 'MULTIPLIER', value: 2 },
  { type: 'MULTIPLIER', value: 0 },
  { type: 'MULTIPLIER', value: -1 },
];
