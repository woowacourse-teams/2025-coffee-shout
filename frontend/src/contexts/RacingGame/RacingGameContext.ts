import { RacingGameData, RacingGameState } from '@/types/miniGame/racingGame';
import { createContextSelector } from '@/utils/createContextSelector';

type RacingGameContextType = {
  racingGameState: RacingGameState;
  racingGameData: RacingGameData;
};
const { Provider, useContextSelector } = createContextSelector<RacingGameContextType>();

export const RacingGameProvider = Provider;

export const useRacingGameState = () => useContextSelector((state) => state.racingGameState);
export const useRacingGameData = () => useContextSelector((state) => state.racingGameData);

export const useRacingGame = () => {
  const racingGameState = useRacingGameState();
  const racingGameData = useRacingGameData();
  return { racingGameState, racingGameData };
};
