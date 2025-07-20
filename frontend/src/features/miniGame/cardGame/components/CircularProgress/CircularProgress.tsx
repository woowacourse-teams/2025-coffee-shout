import * as S from './CircularProgress.styled';

type Props = {
  current: number;
  total: number;
  size?: string;
};

const CircularProgress = ({ current, total, size = '2rem' }: Props) => {
  const progress = total > 0 ? (total - current) / total : 0;
  const radius = 45;
  const circumference = 2 * Math.PI * radius;
  const strokeDashoffset = circumference * (1 - progress);

  return (
    <S.Container $size={size}>
      <S.ProgressRing width="100%" height="100%" viewBox="0 0 100 100">
        <S.BackgroundCircle cx="50" cy="50" r={radius} fill="none" />
        <S.ProgressCircle
          cx="50"
          cy="50"
          r={radius}
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
