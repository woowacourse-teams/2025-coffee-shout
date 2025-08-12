import Headline2 from '@/components/@common/Headline2/Headline2';
import * as S from './Slide.styled';
import { ImgHTMLAttributes, ReactElement } from 'react';

type Props = {
  textLines: string[];
  image: ReactElement<ImgHTMLAttributes<HTMLImageElement>, 'img'>;
  className: string;
};

const Slide = ({ textLines, image, className }: Props) => {
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

export default Slide;
