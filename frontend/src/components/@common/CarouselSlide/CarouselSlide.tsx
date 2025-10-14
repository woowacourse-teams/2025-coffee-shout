import { ReactNode } from 'react';
import Headline2 from '@/components/@common/Headline2/Headline2';
import * as S from './CarouselSlide.styled';

type Props = {
  title: string;
  children: ReactNode;
};

const CarouselSlide = ({ title, children }: Props) => {
  return (
    <S.Container>
      <S.Title>
        <Headline2 color="white">{title}</Headline2>
      </S.Title>
      <S.Content>{children}</S.Content>
    </S.Container>
  );
};

export default CarouselSlide;
