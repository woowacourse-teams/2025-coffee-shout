import { ReactNode } from 'react';
import * as S from './FadeInItem.styled';

type Props = {
  children: ReactNode;
  index?: number;
  delay?: number;
  duration?: number;
};

const FadeInItem = ({ children, index = 0, delay = 200, duration = 600 }: Props) => {
  return (
    <S.Wrapper $index={index} $delay={delay} $duration={duration}>
      {children}
    </S.Wrapper>
  );
};

export default FadeInItem;
