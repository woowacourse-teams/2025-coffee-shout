import RacingLineImg from '@/assets/racing-line.png';
import * as S from './RacingLine.styled';

interface RacingLineProps {
  position: number; // 라인의 절대 위치 (0 = 출발선, 1000 = 도착선)
  myPosition: number; // 내 현재 위치
}

const RacingLine = ({ position, myPosition }: RacingLineProps) => {
  const relativeX = position - myPosition;

  return (
    <S.Container $position={relativeX}>
      <S.Image src={RacingLineImg} />
    </S.Container>
  );
};

export default RacingLine;
