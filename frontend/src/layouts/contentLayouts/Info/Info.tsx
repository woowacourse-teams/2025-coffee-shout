import { PropsWithChildren } from 'react';
import * as S from './Info.styled';

const Info = ({ children }: PropsWithChildren) => {
  return <S.Container>{children}</S.Container>;
};

export default Info;
