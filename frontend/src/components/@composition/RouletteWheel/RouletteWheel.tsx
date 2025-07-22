import * as S from './RouletteWheel.styled';

type Props = {
  spinning?: boolean;
};

const RouletteWheel = ({ spinning = false }: Props) => {
  return (
    <S.Container>
      <S.Wrapper $spinning={spinning}>
        <S.CenterImage src="./images/profile-red.svg" alt="roulette-center" />
      </S.Wrapper>
    </S.Container>
  );
};

export default RouletteWheel;
