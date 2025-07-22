import { useEffect, useState } from 'react';
import * as S from './CircularProgress.styled';

type Props = {
  current: number;
  total: number;
  size?: string;
};

const RADIUS = 45;
const circumference = 2 * Math.PI * RADIUS;

const CircularProgress = ({ current, total, size = '2rem' }: Props) => {
  const [strokeDashoffset, setStrokeDashoffset] = useState(circumference);

  useEffect(() => {
    if (total <= 0) {
      setStrokeDashoffset(circumference);
      return;
    }

    const progress = Math.min(1, (total - current + 1) / total);
    const newStrokeDashoffset = circumference * (1 - progress);
    setStrokeDashoffset(newStrokeDashoffset);
  }, [current, total]);

  return (
    <S.Container $size={size}>
      <S.ProgressRing width="100%" height="100%" viewBox="0 0 100 100">
        <S.BackgroundCircle cx="50" cy="50" r={RADIUS} fill="none" />
        <S.ProgressCircle
          cx="50"
          cy="50"
          r={RADIUS}
          fill="none"
          strokeDasharray={circumference}
          strokeDashoffset={strokeDashoffset}
          transform="rotate(-90 50 50)"
        />
      </S.ProgressRing>
      <S.CountText>{current}</S.CountText>
    </S.Container>
  );
};

export default CircularProgress;
