import * as S from './RouletteWheel.styled';

type Props = {
  spinning?: boolean;
};

const RouletteWheel = ({ spinning = false }: Props) => {
  return (
    <S.Container>
      <S.Wrapper $spinning={spinning}>
        <img src="/images/profile-red.svg" alt="Profile" style={{ width: 64, height: 64 }} />
      </S.Wrapper>
    </S.Container>
  );
};

export default RouletteWheel;
