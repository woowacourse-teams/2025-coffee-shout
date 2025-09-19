import { useProbabilityHistory } from '@/contexts/ProbabilityHistory/ProbabilityHistoryContext';
import { useRouletteTransition } from '@/features/roulette/hooks/useRouletteTransition';
import RouletteWheel from '@/features/roulette/components/RouletteWheel/RouletteWheel';

export const AnimatedRouletteWheel = ({
  finalRotation,
  isSpinning,
}: {
  finalRotation: number;
  isSpinning: boolean;
}) => {
  const { probabilityHistory } = useProbabilityHistory();
  const animatedSectors = useRouletteTransition(
    probabilityHistory.prev,
    probabilityHistory.current
  );

  if (!animatedSectors) return null;

  return (
    <RouletteWheel
      sectors={animatedSectors}
      finalRotation={finalRotation}
      isSpinning={isSpinning}
    />
  );
};

export default AnimatedRouletteWheel;
