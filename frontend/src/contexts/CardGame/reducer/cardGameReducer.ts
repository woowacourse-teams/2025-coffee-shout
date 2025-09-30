import { CardGameState, CardInfo } from '@/types/miniGame/cardGame';
import { RoundType } from '@/types/miniGame/round';

export type State = {
  isTransition: boolean;
  currentRound: RoundType;
  currentCardGameState: CardGameState;
  cardInfos: CardInfo[];
};

export type Action =
  | { type: 'PREPARE'; payload: { cardInfos: CardInfo[] } }
  | { type: 'PLAYING'; payload: { cardInfos: CardInfo[]; round: RoundType } }
  | { type: 'SCORE_BOARD'; payload: { cardInfos: CardInfo[]; round: RoundType } }
  | { type: 'LOADING'; payload: { round: RoundType } }
  | { type: 'DONE' };

export const initialState: State = {
  isTransition: false,
  currentRound: 'FIRST',
  currentCardGameState: 'READY',
  cardInfos: [],
};

export const cardGameReducer = (state: State, action: Action): State => {
  switch (action.type) {
    case 'PREPARE':
      return {
        ...state,
        currentCardGameState: 'PREPARE',
        cardInfos: action.payload.cardInfos,
      };

    case 'PLAYING':
      return {
        ...state,
        currentCardGameState: 'PLAYING',
        cardInfos: action.payload.cardInfos,
        currentRound: action.payload.round,
        isTransition: action.payload.round === 'SECOND' ? false : state.isTransition,
      };

    case 'SCORE_BOARD':
      return {
        ...state,
        currentCardGameState: 'SCORE_BOARD',
        cardInfos: action.payload.cardInfos,
        currentRound: action.payload.round,
      };

    case 'LOADING':
      if (action.payload.round === 'SECOND') {
        return {
          ...state,
          currentCardGameState: 'LOADING',
          currentRound: action.payload.round,
          isTransition: true,
        };
      }
      return state;

    case 'DONE':
      return {
        ...state,
        currentCardGameState: 'DONE',
      };

    default:
      return state;
  }
};
