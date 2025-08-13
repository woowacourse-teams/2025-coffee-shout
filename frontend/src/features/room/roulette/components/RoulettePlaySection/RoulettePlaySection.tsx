import Headline4 from '@/components/@common/Headline4/Headline4';
import RouletteWheel from '@/features/roulette/components/RouletteWheel/RouletteWheel';
import * as S from './RoulettePlaySection.styled';
import { useProbabilityHistory } from '@/contexts/ProbabilityHistory/ProbabilityHistoryContext';
import { useRouletteTransition } from '@/features/roulette/hooks/useRouletteTransition';
import { useIdentifier } from '@/contexts/Identifier/IdentifierContext';
import { convertProbabilitiesToAngles } from '@/features/roulette/utils/convertProbabilitiesToAngles';
import { calculateFinalRotation } from '../../utils/calculateFinalRotation';

type Props = {
  isSpinning: boolean;
  winner: string | null;
  randomAngle: number;
};

const RoulettePlaySection = ({ isSpinning, winner, randomAngle }: Props) => {
  const { myName } = useIdentifier();
  const { probabilityHistory } = useProbabilityHistory();
  const animatedSectors = useRouletteTransition(
    probabilityHistory.prev,
    probabilityHistory.current
  );

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

  if (!animatedSectors) return null;

  return (
    <S.Container>
      <RouletteWheel
        isSpinning={isSpinning}
        sectors={animatedSectors}
        finalRotation={finalRotation}
      />
      <S.ProbabilityText>
        <Headline4>
          당첨 확률 {myProbabilityChange >= 0 ? '+' : ''}
          {myProbabilityChange}%
        </Headline4>
      </S.ProbabilityText>
    </S.Container>
  );
};

export default RoulettePlaySection;
