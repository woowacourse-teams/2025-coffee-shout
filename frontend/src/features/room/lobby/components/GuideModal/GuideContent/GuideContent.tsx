import * as S from './GuideContent.styled';
import { GuideInfo } from '../GuideModal';
import Headline4 from '@/components/@common/Headline4/Headline4';

type GuideContentProps = {
  pageData: GuideInfo;
};

const GuideContent = ({ pageData }: GuideContentProps) => {
  return (
    <S.ContentContainer>
      <S.ImageContainer>
        <S.PlaceholderImage>{pageData.image}</S.PlaceholderImage>
      </S.ImageContainer>
      <S.TextContainer>
        <Headline4>{pageData.title}</Headline4>
        <S.DescriptionWrapper>
          {pageData.description.split('\n').map((line, index) => (
            <S.Description key={index}>{line}</S.Description>
          ))}
        </S.DescriptionWrapper>
      </S.TextContainer>
    </S.ContentContainer>
  );
};

export default GuideContent;
