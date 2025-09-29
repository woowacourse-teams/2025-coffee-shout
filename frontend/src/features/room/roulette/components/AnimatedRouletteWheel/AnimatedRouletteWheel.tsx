import { useProbabilityHistory } from '@/contexts/ProbabilityHistory/ProbabilityHistoryContext';
import { useRouletteTransition } from '@/features/roulette/hooks/useRouletteTransition';
import RouletteWheel from '@/features/roulette/components/RouletteWheel/RouletteWheel';
import { useEffect } from 'react';

type Props = {
  finalRotation: number;
  isSpinning: boolean;
  startAnimation: boolean;
};

export const AnimatedRouletteWheel = ({ finalRotation, isSpinning, startAnimation }: Props) => {
  const { probabilityHistory } = useProbabilityHistory();

  const {
    animatedSectors,
    startAnimation: startAnimationTransition,
    setToPrev,
  } = useRouletteTransition(probabilityHistory.prev, probabilityHistory.current);

  useEffect(() => {
    if (startAnimation) {
      setToPrev();
      setTimeout(() => {
        startAnimationTransition();
      }, 500);
    }
  }, [startAnimation, startAnimationTransition, setToPrev]);

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
