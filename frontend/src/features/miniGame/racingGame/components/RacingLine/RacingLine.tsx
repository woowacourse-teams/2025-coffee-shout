import RacingLineImg from '@/assets/racing-line.png';
import * as S from './RacingLine.styled';

interface RacingLineProps {
  x: number; // 라인의 절대 위치 (0 = 출발선, 1000 = 도착선)
  myX: number; // 내 현재 위치
}

const RacingLine = ({ x, myX }: RacingLineProps) => {
  const relativeX = x - myX;

  return (
    <S.Container $x={relativeX}>
      <S.Image src={RacingLineImg} />
    </S.Container>
  );
};

export default RacingLine;
