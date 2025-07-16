import { PropsWithChildren } from 'react';
import * as S from './Headline1.styled';

type Props = PropsWithChildren;

const Headline1 = ({ children }: Props) => {
  return <S.Container>{children}</S.Container>;
};

export default Headline1;
