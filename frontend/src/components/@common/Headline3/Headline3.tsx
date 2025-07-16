import { PropsWithChildren } from 'react';
import * as S from './Headline3.styled';

type Props = PropsWithChildren;

const Headline3 = ({ children }: Props) => {
  return <S.Container>{children}</S.Container>;
};

export default Headline3;
