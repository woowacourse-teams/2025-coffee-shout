import * as S from './RoulettePlaySection.styled';
import { useProbabilityHistory } from '@/contexts/ProbabilityHistory/ProbabilityHistoryContext';
import { convertProbabilitiesToAngles } from '@/features/roulette/utils/convertProbabilitiesToAngles';
import { calculateFinalRotation } from '../../utils/calculateFinalRotation';
import { AnimatedRouletteWheel } from '../AnimatedRouletteWheel/AnimatedRouletteWheel';
import ProbabilitiesText from '../ProbabilitiesText/ProbabilitiesText';
import RouletteWheelBack from '@/features/roulette/components/RouletteWheelBack/RouletteWheelBack';

type Props = {
  isSpinning: boolean;
  winner: string | null;
  randomAngle: number;
  isProbabilitiesLoading: boolean;
};

const RoulettePlaySection = ({
  isSpinning,
  winner,
  randomAngle,
  isProbabilitiesLoading,
}: Props) => {
  const { probabilityHistory } = useProbabilityHistory();

  const shouldComputeFinalRotation = isSpinning && winner;
  const finalRotation = shouldComputeFinalRotation
    ? calculateFinalRotation({
        finalAngles: convertProbabilitiesToAngles(probabilityHistory.current),
        winner,
        randomAngle,
      })
    : 0;

  return (
    <S.Container>
      <S.RouletteWheelWrapper>
        <S.FlipWheelWrapper>
          <S.Flipper flipped={!isProbabilitiesLoading}>
            <S.Front>
              <AnimatedRouletteWheel
                finalRotation={finalRotation}
                isSpinning={isSpinning}
                startAnimation={!isProbabilitiesLoading}
              />
            </S.Front>
            <S.Back>
              <RouletteWheelBack />
            </S.Back>
          </S.Flipper>
        </S.FlipWheelWrapper>
      </S.RouletteWheelWrapper>
      <ProbabilitiesText isProbabilitiesLoading={isProbabilitiesLoading} />
    </S.Container>
  );
};

export default RoulettePlaySection;
