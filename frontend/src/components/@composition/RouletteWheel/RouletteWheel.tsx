import * as S from './RouletteWheel.styled';

interface RouletteWheelProps {
  spinning: boolean;
}

const RouletteWheel = ({ spinning }: RouletteWheelProps) => {
  return (
    <S.Container>
      <S.Wrapper $spinning={spinning}>
        <img src="/images/profile-red.svg" alt="Profile" style={{ width: 64, height: 64 }} />
      </S.Wrapper>
    </S.Container>
  );
};

export default RouletteWheel;
