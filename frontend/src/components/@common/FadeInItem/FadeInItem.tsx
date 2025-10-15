import { ReactNode } from 'react';
import * as S from './FadeInItem.styled';

type Props = {
  children: ReactNode;
  index?: number;
  delay?: number;
  duration?: number;
};

const FadeInItem = ({ children, index = 0, delay = 0.2, duration = 0.6 }: Props) => {
  return (
    <S.Wrapper $index={index} $delay={delay} $duration={duration}>
      {children}
    </S.Wrapper>
  );
};

export default FadeInItem;
