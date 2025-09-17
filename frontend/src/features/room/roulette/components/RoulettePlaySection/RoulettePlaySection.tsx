import Headline4 from '@/components/@common/Headline4/Headline4';
import * as S from './RoulettePlaySection.styled';
import { useProbabilityHistory } from '@/contexts/ProbabilityHistory/ProbabilityHistoryContext';
import { useIdentifier } from '@/contexts/Identifier/IdentifierContext';
import { convertProbabilitiesToAngles } from '@/features/roulette/utils/convertProbabilitiesToAngles';
import { calculateFinalRotation } from '../../utils/calculateFinalRotation';
import { AnimatedRouletteWheel } from '../AnimatedRouletteWheel/AnimatedRouletteWheel';

type Props = {
  isSpinning: boolean;
  winner: string | null;
  randomAngle: number;
};

const formatPercent = new Intl.NumberFormat('ko-KR', {
  minimumFractionDigits: 2,
  maximumFractionDigits: 2,
});

const RoulettePlaySection = ({ isSpinning, winner, randomAngle }: Props) => {
  const { myName } = useIdentifier();
  const { probabilityHistory } = useProbabilityHistory();

  const myPrevProbability =
    probabilityHistory.prev.find((player) => player.playerName === myName)?.probability ?? 0;
  const myCurrentProbability =
    probabilityHistory.current.find((player) => player.playerName === myName)?.probability ?? 0;

  const myProbabilityChange = myCurrentProbability - myPrevProbability;

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
        <AnimatedRouletteWheel finalRotation={finalRotation} isSpinning={isSpinning} />
      </S.RouletteWheelWrapper>
      <S.ProbabilityText>
        <Headline4>
          현재 확률 : {myCurrentProbability + '%'} {'('}
          <S.ProbabilityChange isPositive={myProbabilityChange >= 0}>
            {(myProbabilityChange >= 0 ? '+' : '') +
              formatPercent.format(myProbabilityChange) +
              '%'}
          </S.ProbabilityChange>
          {')'}
        </Headline4>
      </S.ProbabilityText>
    </S.Container>
  );
};

export default RoulettePlaySection;
