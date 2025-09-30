import Headline2 from '@/components/@common/Headline2/Headline2';
import { ComponentProps, ReactElement } from 'react';
import * as S from './GameIntroSlide.styled';

type Props = {
  textLines: readonly string[];
  image: ReactElement<ComponentProps<'img'>>;
  className: string;
};

const GameIntroSlide = ({ textLines, image, className }: Props) => {
  return (
    <S.Container className={className}>
      <S.TextWrapper>
        {textLines.map((text, index) => (
          <Headline2 key={index} color="white">
            {text}
          </Headline2>
        ))}
      </S.TextWrapper>
      <S.ImageWrapper>{image}</S.ImageWrapper>
    </S.Container>
  );
};

export default GameIntroSlide;
