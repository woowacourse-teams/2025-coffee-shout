import { PropsWithChildren, useCallback, useState } from 'react';
import { ProbabilityHistory, PlayerProbability } from '@/types/roulette';
import { ProbabilityHistoryContext } from './ProbabilityHistoryContext';

const ProbabilityHistoryProvider = ({ children }: PropsWithChildren) => {
  const [probabilityHistoryState, setProbabilityHistoryState] = useState<ProbabilityHistory>({
    prev: [],
    current: [],
  });

  const updateCurrentProbabilities = useCallback((probabilities: PlayerProbability[]) => {
    setProbabilityHistoryState((prev) => {
      const isEqual = areProbabilitiesEqual(prev.current, probabilities);
      if (isEqual) {
        return prev;
      }

      return {
        prev: prev.current,
        current: probabilities,
      };
    });
  }, []);

  const clearProbabilityHistory = useCallback(() => {
    setProbabilityHistoryState({
      prev: [],
      current: [],
    });
  }, []);

  return (
    <ProbabilityHistoryContext.Provider
      value={{
        probabilityHistory: probabilityHistoryState,
        updateCurrentProbabilities,
        clearProbabilityHistory,
      }}
    >
      {children}
    </ProbabilityHistoryContext.Provider>
  );
};

export default ProbabilityHistoryProvider;

const areProbabilitiesEqual = (arr1: PlayerProbability[], arr2: PlayerProbability[]): boolean => {
  if (arr1.length !== arr2.length) {
    return false;
  }

  if (arr1.length === 0) {
    return true;
  }

  return arr1.every((item1, index) => {
    const item2 = arr2[index];
    return (
      item1.playerName === item2.playerName &&
      item1.probability === item2.probability &&
      item1.playerColor === item2.playerColor
    );
  });
};
