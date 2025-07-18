import { PropsWithChildren } from 'react';
import * as S from './Title.styled';

const Title = ({ children }: PropsWithChildren) => {
  return <S.Container>{children}</S.Container>;
};

export default Title;
