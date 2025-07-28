export type CardGameState = 'WAITING' | 'PLAYING' | 'FINISHED';

export type CardType = 'ADDITION' | 'MULTIPLIER';

export interface CardInfoMessage {
  cardType: CardType;
  value: number;
  selected: boolean;
  playerName: string | null;
}

export interface CardGameStateResponse {
  cardGameState: CardGameState;
  currentRound: number;
  cardInfoMessages: CardInfoMessage[];
  allSelected: boolean;
}
