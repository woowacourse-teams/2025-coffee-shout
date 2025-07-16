import { PropsWithChildren } from 'react';
import * as S from './Headline4.styled';

type Props = PropsWithChildren;

const Headline4 = ({ children }: Props) => {
  return <S.Container>{children}</S.Container>;
};

export default Headline4;
