import * as S from './ProgressCounter.styled';

type Props = {
  current: number;
  total: number;
};

const ProgressCounter = ({ current, total }: Props) => {
  return (
    <S.Container>
      {current}/{total}
    </S.Container>
  );
};

export default ProgressCounter;
