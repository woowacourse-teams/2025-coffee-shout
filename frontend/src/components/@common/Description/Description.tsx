import { PropsWithChildren } from 'react';
import * as S from './Description.styled';

type Props = PropsWithChildren;

const Description = ({ children }: Props) => {
  return <S.Container>{children}</S.Container>;
};

export default Description;
