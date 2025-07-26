import Headline4 from '@/components/@common/Headline4/Headline4';
import RouletteWheel from '@/features/roulette/components/RouletteWheel/RouletteWheel';
import * as S from './RoulettePlaySection.styled';

const RoulettePlaySection = ({ isSpinning }: { isSpinning: boolean }) => {
  return (
    <S.Container>
      <RouletteWheel spinning={isSpinning} />
      <S.ProbabilityText>
        <Headline4>당첨 확률 +10%</Headline4>
      </S.ProbabilityText>
    </S.Container>
  );
};

export default RoulettePlaySection;
