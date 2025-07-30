import Headline4 from '@/components/@common/Headline4/Headline4';
import RouletteWheel from '@/features/roulette/components/RouletteWheel/RouletteWheel';
import * as S from './RoulettePlaySection.styled';
import { mockPlayers } from '@/features/room/lobby/components/RouletteSection/RouletteSection';

const RoulettePlaySection = ({ isSpinning }: { isSpinning: boolean }) => {
  return (
    <S.Container>
      <RouletteWheel isSpinning={isSpinning} players={mockPlayers} />
      <S.ProbabilityText>
        <Headline4>당첨 확률 +10%</Headline4>
      </S.ProbabilityText>
    </S.Container>
  );
};

export default RoulettePlaySection;
