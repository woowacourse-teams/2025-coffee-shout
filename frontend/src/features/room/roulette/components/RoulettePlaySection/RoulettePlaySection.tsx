import Headline4 from '@/components/@common/Headline4/Headline4';
import RouletteWheel from '@/features/roulette/components/RouletteWheel/RouletteWheel';
import * as S from './RoulettePlaySection.styled';
import { useProbabilityHistory } from '@/contexts/ProbabilityHistory/ProbabilityHistoryContext';
import { useRouletteTransition } from '@/features/roulette/hooks/useRouletteTrantision';
import { useIdentifier } from '@/contexts/Identifier/IdentifierContext';

type Props = {
  isSpinning: boolean;
};

const formatPercent = new Intl.NumberFormat('ko-KR', {
  minimumFractionDigits: 2,
  maximumFractionDigits: 2,
});

const RoulettePlaySection = ({ isSpinning }: Props) => {
  const { myName } = useIdentifier();
  const { probabilityHistory } = useProbabilityHistory();
  const angles = useRouletteTransition(probabilityHistory.prev, probabilityHistory.current);

  const myPrevProbability =
    probabilityHistory.prev.find((player) => player.playerName === myName)?.probability ?? 0;
  const myCurrentProbability =
    probabilityHistory.current.find((player) => player.playerName === myName)?.probability ?? 0;

  const myProbabilityChange = myCurrentProbability - myPrevProbability;

  if (!angles) return null;

  return (
    <S.Container>
      <RouletteWheel
        isSpinning={isSpinning}
        angles={angles}
        playerProbabilities={probabilityHistory.current}
      />
      <S.ProbabilityText>
        <Headline4>
          당첨 확률 {myProbabilityChange >= 0 ? '+' : ''}
          {formatPercent.format(myProbabilityChange)}%
        </Headline4>
      </S.ProbabilityText>
    </S.Container>
  );
};

export default RoulettePlaySection;
