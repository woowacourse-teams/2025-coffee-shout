export type AdditionValue = -40 | -30 | -20 | -10 | 0 | 10 | 20 | 30 | 40;
export type MultiplierValue = -1 | 0 | 2 | 4;

export type CardType = 'addition' | 'multiplier';

export type AdditionCard = {
  type: 'addition';
  value: AdditionValue;
};

export type MultiplierCard = {
  type: 'multiplier';
  value: MultiplierValue;
};

export type Card = AdditionCard | MultiplierCard;

export const GENERAL_CARDS: AdditionCard[] = [
  { type: 'addition', value: -40 },
  { type: 'addition', value: -30 },
  { type: 'addition', value: -20 },
  { type: 'addition', value: -10 },
  { type: 'addition', value: 0 },
  { type: 'addition', value: 10 },
  { type: 'addition', value: 20 },
  { type: 'addition', value: 30 },
  { type: 'addition', value: 40 },
];

export const SPECIAL_CARDS: MultiplierCard[] = [
  { type: 'multiplier', value: 4 },
  { type: 'multiplier', value: 2 },
  { type: 'multiplier', value: 0 },
  { type: 'multiplier', value: -1 },
];
