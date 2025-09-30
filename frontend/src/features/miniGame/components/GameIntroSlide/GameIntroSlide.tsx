import Headline2 from '@/components/@common/Headline2/Headline2';
import * as S from './GameIntroSlide.styled';

type Props = {
  textLines: readonly string[];
  imageSrc: string;
  className: string;
};

const GameIntroSlide = ({ textLines, imageSrc, className }: Props) => {
  return (
    <S.Container className={className}>
      <S.TextWrapper>
        {textLines.map((text, index) => (
          <Headline2 key={index} color="white">
            {text}
          </Headline2>
        ))}
      </S.TextWrapper>
      <S.ImageWrapper>
        <S.Image src={imageSrc} />
      </S.ImageWrapper>
    </S.Container>
  );
};

export default GameIntroSlide;
