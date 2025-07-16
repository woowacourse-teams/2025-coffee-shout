import { PropsWithChildren } from 'react';
import * as S from './Headline2.styled';

type Props = PropsWithChildren;

const Headline2 = ({ children }: Props) => {
  return <S.Container>{children}</S.Container>;
};

export default Headline2;
