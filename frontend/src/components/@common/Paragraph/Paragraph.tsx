import { PropsWithChildren } from 'react';
import * as S from './Paragraph.styled';

type Props = PropsWithChildren;

const Paragraph = ({ children }: Props) => {
  return <S.Container>{children}</S.Container>;
};

export default Paragraph;
