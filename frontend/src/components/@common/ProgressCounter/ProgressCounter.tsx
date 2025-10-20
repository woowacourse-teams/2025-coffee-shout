import ScreenReaderOnly from '../ScreenReaderOnly/ScreenReaderOnly';
import * as S from './ProgressCounter.styled';

type Props = {
  current: number;
  total: number;
};

const ProgressCounter = ({ current, total }: Props) => {
  return (
    <S.Container>
      <span aria-hidden="true">
        {current}/{total}
      </span>
      <ScreenReaderOnly>{`전체 ${total}명 중 ${current}명 참가중`}</ScreenReaderOnly>
    </S.Container>
  );
};

export default ProgressCounter;
