import RacingLineImg from '@/assets/racing-line.png';
import * as S from './RacingLine.styled';

type RacingLineProps = {
  position: number;
  myPosition: number;
};

const RacingLine = ({ position, myPosition }: RacingLineProps) => {
  const relativeX = position - myPosition;

  return (
    <S.Container $position={relativeX}>
      <S.Image src={RacingLineImg} />
    </S.Container>
  );
};

export default RacingLine;
