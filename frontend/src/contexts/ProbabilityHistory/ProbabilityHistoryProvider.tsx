import { PropsWithChildren, useCallback, useState } from 'react';
import { ProbabilityHistory, PlayerProbability } from '@/types/roulette';
import { ProbabilityHistoryContext } from './ProbabilityHistoryContext';

const ProbabilityHistoryProvider = ({ children }: PropsWithChildren) => {
  const [probabilityHistory, setProbabilityHistory] = useState<ProbabilityHistory>({
    prev: [],
    current: [],
  });

  const updateCurrentProbabilities = useCallback((probabilities: PlayerProbability[]) => {
    setProbabilityHistory((prev) => ({
      prev: prev.current,
      current: probabilities,
    }));
  }, []);

  const clearProbabilityHistory = useCallback(() => {
    setProbabilityHistory({
      prev: [],
      current: [],
    });
  }, []);

  return (
    <ProbabilityHistoryContext.Provider
      value={{
        probabilityHistory,
        updateCurrentProbabilities,
        clearProbabilityHistory,
      }}
    >
      {children}
    </ProbabilityHistoryContext.Provider>
  );
};

export default ProbabilityHistoryProvider;
