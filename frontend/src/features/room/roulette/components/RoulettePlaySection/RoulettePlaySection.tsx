import Headline4 from '@/components/@common/Headline4/Headline4';
import RouletteWheel from '@/features/roulette/components/RouletteWheel/RouletteWheel';
import * as S from './RoulettePlaySection.styled';
import { useProbabilityHistory } from '@/contexts/ProbabilityHistory/ProbabilityHistoryContext';
import { useRouletteTransition } from '@/features/roulette/hooks/useRouletteTrantision';

type Props = {

  isSpinning: boolean;
};

const RoulettePlaySection = ({ isSpinning }: Props) => {
  const { probabilityHistory } = useProbabilityHistory();
  const angles = useRouletteTransition(probabilityHistory.prev, probabilityHistory.current);

  if (!angles) return null;

  return (
    <S.Container>
      <RouletteWheel isSpinning={isSpinning} angles={angles} />
      <S.ProbabilityText>
        <Headline4>당첨 확률 +10%</Headline4>
      </S.ProbabilityText>
    </S.Container>
  );
};

export default RoulettePlaySection;
